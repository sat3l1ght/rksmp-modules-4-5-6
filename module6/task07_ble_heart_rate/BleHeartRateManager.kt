package com.example.module6.task07

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object HeartRateUuids {
    val HEART_RATE_SERVICE: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
    val HEART_RATE_MEASUREMENT: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
    val CCC_DESCRIPTOR: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
}

data class BleDeviceUi(val name: String, val address: String, val device: BluetoothDevice)

class BleHeartRateManager(private val context: Context) {
    private val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? get() = btManager.adapter
    private var gatt: BluetoothGatt? = null

    private val _devices = MutableStateFlow<List<BleDeviceUi>>(emptyList())
    val devices: StateFlow<List<BleDeviceUi>> = _devices.asStateFlow()

    private val _connectionState = MutableStateFlow("Disconnected")
    val connectionState: StateFlow<String> = _connectionState.asStateFlow()

    private val _heartRateBpm = MutableStateFlow<Int?>(null)
    val heartRateBpm: StateFlow<Int?> = _heartRateBpm.asStateFlow()

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val dev = result.device
            val item = BleDeviceUi(dev.name ?: "Unknown BLE", dev.address, dev)
            if (_devices.value.none { it.address == item.address }) {
                _devices.value = _devices.value + item
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        stopScan()
        _devices.value = emptyList()
        adapter?.bluetoothLeScanner?.startScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        adapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        stopScan()
        _connectionState.value = "Connecting"
        gatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    _connectionState.value = "Connected"
                    g.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    _connectionState.value = "Disconnected"
                    _heartRateBpm.value = null
                }
            }

            override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
                val service = g.getService(HeartRateUuids.HEART_RATE_SERVICE) ?: return
                val characteristic = service.getCharacteristic(HeartRateUuids.HEART_RATE_MEASUREMENT) ?: return
                g.setCharacteristicNotification(characteristic, true)
                val desc = characteristic.getDescriptor(HeartRateUuids.CCC_DESCRIPTOR) ?: return
                desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                g.writeDescriptor(desc)
            }

            override fun onCharacteristicChanged(
                g: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                if (characteristic.uuid == HeartRateUuids.HEART_RATE_MEASUREMENT && value.isNotEmpty()) {
                    val flags = value[0].toInt()
                    val isUint16 = (flags and 0x01) != 0
                    val bpm = if (isUint16 && value.size >= 3) {
                        ((value[2].toInt() and 0xFF) shl 8) or (value[1].toInt() and 0xFF)
                    } else if (value.size >= 2) {
                        value[1].toInt() and 0xFF
                    } else null
                    _heartRateBpm.value = bpm
                }
            }
        })
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        _connectionState.value = "Disconnected"
        _heartRateBpm.value = null
    }
}
