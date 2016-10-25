package com.seoul.kysersozelee.adbController.adb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

public class AdbServerCommunicator {
	private final String ADB_ADDRESS_HOST = "localhost";
	private final int ADB_ADDRESS_PORT = 5037;
	private final String HOST_TRANSPORT = "host:transport:%s";

	private String deviceId;
	Socket socket;

	public AdbServerCommunicator(String deviceId) throws IOException, InterruptedException, ExecutionException {
		this.deviceId = deviceId;
		this.connectGlobalSocket();
	}

	public AdbResult sendHostMessage(String message) throws IOException {
		AdbResult adbResult = new AdbResult();
		String recvMessage = null;

		try {
			DataOutputStream os = new DataOutputStream(this.socket.getOutputStream());
			os.writeBytes(this.messageToAdbProtocolMessage(message));

			if (message.contains(HOST_TRANSPORT.replace("%s", ""))) {
				this.checkOK(new DataInputStream(this.socket.getInputStream()));
			} else if (!message.contains(HOST_TRANSPORT.replace("%s", ""))) {
				recvMessage = this.getMessageFromInputStream(new DataInputStream(this.socket.getInputStream()));
				if (recvMessage != null && recvMessage.length() > 4) {
					int chunkSize = this.getChunkSize(recvMessage.substring(0, 4));
					adbResult.setSuccess(true);
					if (chunkSize == 0)
						adbResult.setData(null);
					else
						adbResult.setData(recvMessage.substring(4, 4 + chunkSize - 1));
				} else if (recvMessage == null) {
					adbResult.setSuccess(false);
					adbResult.setData(null);
				}
			}
		} catch (SocketException e) {
			adbResult.setSuccess(false);
			adbResult.setData(e.getMessage());
			return adbResult;
		} catch (IOException e) {
			adbResult.setSuccess(false);
			adbResult.setData(e.getMessage());
			return adbResult;
		} finally {
			if (!message.contains(HOST_TRANSPORT.replace("%s", ""))) {
				this.connectGlobalSocket();
			}
		}

		return adbResult;
	}

	public AdbResult sendDeviceMessage(String message) throws IOException {
		AdbResult adbResult = new AdbResult();
		String recvMessage = null;

		try {
			this.sendHostMessage(String.format(HOST_TRANSPORT, this.deviceId));

			DataOutputStream os = new DataOutputStream(this.socket.getOutputStream());
			os.writeBytes(this.messageToAdbProtocolMessage(message));

			recvMessage = this.getMessageFromInputStream(new DataInputStream(this.socket.getInputStream()));

			adbResult.setSuccess(true);
			adbResult.setData(recvMessage);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			this.connectGlobalSocket();
		}

		return adbResult;
	}

	public void connectGlobalSocket() throws IOException {
		if (this.socket != null && !this.socket.isClosed()) {
			this.socket.close();
		}
		this.socket = new Socket(ADB_ADDRESS_HOST, ADB_ADDRESS_PORT);
		this.socket.setSoTimeout(3000);
		this.socket.setSoLinger(true, 0);

	}

	private void setTransport() {

	}

	private String getMessageFromInputStream(DataInputStream is) throws SocketTimeoutException, IOException {
		StringBuilder sb = new StringBuilder();
		try {
			this.checkOK(is);

			String line = null;
			while ((line = is.readLine()) != null) {
				sb.append(line);
			}

		} catch (SocketTimeoutException e) {
			throw e;
		} catch (SocketException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
		return sb.toString();
	}

	public boolean checkOK(DataInputStream is) throws SocketException {
		byte[] recv = new byte[4];
		try {
			is.read(recv, 0, 4);

			String result = new String(recv);
			if (recv.equals("0049")) {
				throw new SocketException(
						"ERROR: This computer is unauthorized. Please check the confirmation dialog on your device.");
			} else if (result.equals("FAIL")) {
				StringBuilder sb = new StringBuilder();
				String responseLine;
				String recvMessage;
				while ((responseLine = is.readLine()) != null) {
					sb.append(responseLine);
				}

				recvMessage = sb.toString();
				int chunkSize = this.getChunkSize(recvMessage.substring(0, 4));

				throw new SocketException("checkOK - message : " + recvMessage.substring(4, 4 + chunkSize));
			}

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	private String messageToAdbProtocolMessage(String message) {
		return String.format("%04X%s", message.length(), message);
	}

	private boolean isOkay(String msg) {
		if ("OKAY".equals(msg)) {
			return true;
		} else {
			return false;
		}
	}

	public int getChunkSize(String chunkData) throws IOException {
		return Integer.parseUnsignedInt(new String(chunkData), 16);
	}

}
