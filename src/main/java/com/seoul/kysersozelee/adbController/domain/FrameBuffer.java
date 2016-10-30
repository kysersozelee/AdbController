package com.seoul.kysersozelee.adbController.domain;

import java.awt.image.BufferedImage;

public class FrameBuffer {

	int version;
	int bpp;
	int size;
	int width;
	int height;
	int roffset;
	int rlen;
	int boffset;
	int blen;
	int goffset;
	int glen;
	int aoffset;
	int alen;
	byte[] imageByteArr;
	BufferedImage image;

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getBpp() {
		return bpp;
	}

	public void setBpp(int bpp) {
		this.bpp = bpp;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getRoffset() {
		return roffset;
	}

	public void setRoffset(int roffset) {
		this.roffset = roffset;
	}

	public int getRlen() {
		return rlen;
	}

	public void setRlen(int rlen) {
		this.rlen = rlen;
	}

	public int getBoffset() {
		return boffset;
	}

	public void setBoffset(int boffset) {
		this.boffset = boffset;
	}

	public int getBlen() {
		return blen;
	}

	public void setBlen(int blen) {
		this.blen = blen;
	}

	public int getGoffset() {
		return goffset;
	}

	public void setGoffset(int goffset) {
		this.goffset = goffset;
	}

	public int getGlen() {
		return glen;
	}

	public void setGlen(int glen) {
		this.glen = glen;
	}

	public int getAoffset() {
		return aoffset;
	}

	public void setAoffset(int aoffset) {
		this.aoffset = aoffset;
	}

	public int getAlen() {
		return alen;
	}

	public void setAlen(int alen) {
		this.alen = alen;
	}

	public byte[] getImageByteArr() {
		return imageByteArr;
	}

	public void setImageByteArr(byte[] imageByteArr) {
		this.imageByteArr = imageByteArr;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}
}
