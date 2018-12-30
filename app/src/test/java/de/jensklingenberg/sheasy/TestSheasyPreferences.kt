package de.jensklingenberg.sheasy

import de.jensklingenberg.model.Device
import repository.SheasyPrefDataSource


class TestSheasyPreferences : SheasyPrefDataSource {
    override val acceptAllConnections=true


    override val webSocketPort = 8765


    override val APIV1 = "/api/v1/"

    override val defaultPath = "/storage/emulated/0/"
    override val port = 8766


    override val authorizedDevices = mutableListOf<Device>()

    override fun addAuthorizedDevice(device: Device) {
        authorizedDevices.add(device)
    }

    override fun removeDevice(device: Device) {
        authorizedDevices.remove(device)
    }
}