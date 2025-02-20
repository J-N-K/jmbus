/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus.transportlayer;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


class SerialLayer implements TransportLayer {
    private final int timeout;

    private DataOutputStream os;
    private DataInputStream is;
    private SerialPort serialPort;
    private CommPortIdentifier portIdentifier;
    private SerialBuilder<?, ?> serialBuilder;

    public SerialLayer(int timeout, SerialBuilder<?, ?> serialInfo) {
        this.timeout = timeout;
        this.serialBuilder = serialInfo;
    }

    @Override
    public void open() throws IOException {
        try {
            portIdentifier = CommPortIdentifier.getPortIdentifier(serialBuilder.getSerialPortName());
            serialPort = portIdentifier.open("jMBus", timeout);
            serialPort.setSerialPortParams(serialBuilder.getBaudrate(), serialBuilder.getDataBits(), serialBuilder.getStopBits(),
                    serialBuilder.getParity());
            os = new DataOutputStream(serialPort.getOutputStream());
            is = new DataInputStream(serialPort.getInputStream());
        } catch (PortInUseException | NoSuchPortException | UnsupportedCommOperationException e) {
            throw new IOException(e);
        }
    }

    @Override
    public DataOutputStream getOutputStream() {
        return os;
    }

    @Override
    public DataInputStream getInputStream() {
        return is;
    }

    @Override
    public void close() {
        if (serialPort == null || portIdentifier == null || !portIdentifier.isCurrentlyOwned()) {
            return;
        }

        serialPort.close();
    }

    @Override
    public boolean isClosed() {
        return serialPort == null;
    }

    @Override
    public void setTimeout(int timeout) throws IOException {
        try {
            serialPort.enableReceiveTimeout(timeout);
        } catch (UnsupportedCommOperationException e) {
            throw new IOException(e);
        }
    }

    @Override
    public int getTimeout() {
        return serialPort.getReceiveTimeout();
    }

}
