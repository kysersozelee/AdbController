package com.seoul.kysersozelee.adbController.adb;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutionException;

import com.seoul.kysersozelee.adbController.domain.AdbResult;
import com.seoul.kysersozelee.adbController.domain.FrameBuffer;

public class AdbServerComponent {
	private final String ADB_ADDRESS_HOST = "localhost";
	private final int ADB_ADDRESS_PORT = 5037;
	private final String HOST_TRANSPORT = "host:transport:%s";

	private String serialNumber;
	Socket socket;

	public AdbServerComponent() throws IOException, InterruptedException, ExecutionException {
		this.connectGlobalSocket();
	}

	public AdbServerComponent(String serialNumber) throws IOException, InterruptedException, ExecutionException {
		this.serialNumber = serialNumber;
		this.connectGlobalSocket();
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public FrameBuffer sendFramebufferMessage(String message) {
		if (this.serialNumber == null) {
			return null;
		}

		FrameBuffer framebuffer = new FrameBuffer();
		try {
			this.sendHostMessage(String.format(HOST_TRANSPORT, this.serialNumber));

			DataOutputStream os = new DataOutputStream(this.socket.getOutputStream());
			os.writeBytes(this.convertToAdbProtocolString(message));
			DataInputStream is = new DataInputStream(socket.getInputStream());

			this.checkOK(is);

			LittleEndianDataInputStreamUtil littleEndianDataInputStream = new LittleEndianDataInputStreamUtil(is);

			framebuffer.setVersion(littleEndianDataInputStream.readInt());
			framebuffer.setBpp(littleEndianDataInputStream.readInt());
			framebuffer.setSize(littleEndianDataInputStream.readInt());
			framebuffer.setWidth(littleEndianDataInputStream.readInt());
			framebuffer.setHeight(littleEndianDataInputStream.readInt());
			framebuffer.setRoffset(littleEndianDataInputStream.readInt());
			framebuffer.setRlen(littleEndianDataInputStream.readInt());
			framebuffer.setBoffset(littleEndianDataInputStream.readInt());
			framebuffer.setBlen(littleEndianDataInputStream.readInt());
			framebuffer.setGoffset(littleEndianDataInputStream.readInt());
			framebuffer.setGlen(littleEndianDataInputStream.readInt());
			framebuffer.setAoffset(littleEndianDataInputStream.readInt());
			framebuffer.setAlen(littleEndianDataInputStream.readInt());

			String endMessage = String.format("\0");

			os.writeBytes(this.convertToAdbProtocolString(endMessage));
			byte[] imageByteArr = this.receiveByte(framebuffer.getSize(), is);

			BufferedImage img = new BufferedImage(framebuffer.getWidth(), framebuffer.getHeight(),
					BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < framebuffer.getHeight(); y++) {
				for (int x = 0; x < framebuffer.getWidth(); x++) {
					int index = y * framebuffer.getWidth() + x;
					int red = imageByteArr[index * 4] & 0xFF;
					int green = imageByteArr[index * 4 + 1] & 0xFF;
					int blue = imageByteArr[index * 4 + 2] & 0xFF;
					Color c = new Color(red, green, blue);
					int rgb = c.getRGB();
					img.setRGB(x, y, rgb);
				}
			}
			framebuffer.setImage(img);
			return framebuffer;
		} catch (Exception e) {
			return framebuffer;
		}
	}

	public AdbResult sendHostMessage(String message) throws IOException {
		AdbResult adbResult = new AdbResult();
		String recvMessage = null;

		try {
			DataOutputStream os = new DataOutputStream(this.socket.getOutputStream());
			os.writeBytes(this.convertToAdbProtocolString(message));

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
		if (this.serialNumber == null) {
			return null;
		}

		AdbResult adbResult = new AdbResult();
		String recvMessage = null;

		try {
			this.sendHostMessage(String.format(HOST_TRANSPORT, this.serialNumber));

			DataOutputStream os = new DataOutputStream(this.socket.getOutputStream());
			os.writeBytes(this.convertToAdbProtocolString(message));

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

	private String convertToAdbProtocolString(String message) {
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

	public byte[] receiveByte(int size, DataInputStream is) throws IOException {
		int nr = 0;

		byte[] chunckData = new byte[size];

		while (nr < size) {
			is.read(chunckData, nr, Math.min(size - nr, 4096));
			nr += Math.min(size - nr, 4096);
		}

		return chunckData;
	}
}
