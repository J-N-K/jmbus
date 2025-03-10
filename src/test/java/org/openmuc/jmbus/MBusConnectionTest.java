/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmuc.jmbus.MBusConnection.MBusSerialBuilder;

@RunWith(JUnitParamsRunner.class)
public class MBusConnectionTest {

    @Test
    @Ignore
    public void constructorTest() throws IOException {
        MBusSerialBuilder builder = MBusConnection.newSerialBuilder("/dev/ttyS99");
        MBusConnection mBusConnection = builder.build();
        mBusConnection.close();
    }

    public Object testParserData() {
        Object[] p1 = {1, MessagesData.testMsg1, 1, new int[] {3}, false};
        Object[] p2 = {2, MessagesData.testMsg2, 5, new int[] {6}, false};
        Object[] p3 = {3, MessagesData.testMsg3, 1, new int[] {13}, false};
        Object[] p4 = {4, MessagesData.testMsg4, 0, new int[] {9}, false};
        Object[] p5 = {5, MessagesData.testMsg5, 5, new int[] {10}, false};
        Object[] p6 = {6, MessagesData.testMsg6, 13, new int[] {12}, false};
        Object[] p7 = {7, MessagesData.testMsg7, 1, new int[] {12}, false};
        Object[] p8 = {8, MessagesData.testMsg8, 0, new int[] {10}, false};
        Object[] p9 = {9, MessagesData.testMsg9, 29, new int[] {31}, false};
        Object[] p10 = {10, MessagesData.testMsg10, 93, new int[] {22}, true};
        Object[] p11 = {11, MessagesData.testMsg11, 11, new int[] {7}, false};
        Object[] p12 = {12, MessagesData.test_ABB_A41_messages, 9, MessagesData.test_ABB_A41_DataRecodSizes, false};
        Object[] p13 = {
            13,
            MessagesData.test_Schneider_Electric_message,
            93,
            MessagesData.test_Schneider_Electric_DataRecodSizes,
            false
        };
        return new Object[] {p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13};
    }

    @Test
    public void testListenBeforeTalk() throws IOException {

        byte[] msg = MessagesData.test_ABB_A41_Msg1;

        byte[] garbage = new byte[] {(byte) 0x01, (byte) 0x03, (byte) 0x03, (byte) 0x07};

        byte[] msgWithGarbage = new byte[msg.length + garbage.length];
        System.arraycopy(msg, 0, msgWithGarbage, 0, msg.length);
        System.arraycopy(garbage, 0, msgWithGarbage, msg.length, garbage.length);

        final ByteArrayInputStream is = new ByteArrayInputStream(msgWithGarbage);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        // is.mark(msgWithGarbage.length);
        assertTrue(is.available() > 0);

        MBusTestTCPConnectionBuilder builder = new MBusTestTCPConnectionBuilder("", 0, is, os);
        MBusConnection connection = builder.build();

        VariableDataStructure read = connection.read(42);
        System.out.println(read.toString());
        connection.close();
    }

    @Test
    @Parameters(method = "testParserData")
    public void testMultiMessages(
            int numb, List<byte[]> messages, int addressField, int[] dataRecodSizes, boolean withException)
            throws DecodingException, IOException {
        byte[] msg;

        System.out.println("################### - " + numb + " - ######################");

        for (int i = 0; i <= messages.size() - 1; i++) {
            msg = messages.get(i);
            MBusMessage lpdu = MBusMessage.decode(msg, msg.length);

            assertEquals(addressField, lpdu.getAddressField());

            VariableDataStructure vdr = lpdu.getVariableDataResponse();
            List<DataRecord> dataRecords = vdr.getDataRecords();
            try {
                vdr.decode();
            } catch (Exception e) {
                if (!withException) {
                    throw e;
                }
            }
            assertEquals(dataRecodSizes[i], dataRecords.size());

            System.out.println(vdr.toString() + '\n');

            if (!vdr.moreRecordsFollow()) {
                break;
            }
        }
    }
}
