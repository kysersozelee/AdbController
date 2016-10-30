package com.seoul.kysersozelee.adbController.domain;

import java.awt.Point;

import org.opencv.core.Core.MinMaxLocResult;

public class ImageCompareResult {
	String targetImagePath;
	String templateImagePath;
	String outputImagePath;
	Point maxMatchingPosition;
	double matchingRate;
	MinMaxLocResult minMaxLocResult;

	public ImageCompareResult(String targetImagePath, String templateImagePath, String outputImagePath,
			Point maxMatchingPosition, double matchingRate, MinMaxLocResult minMaxLocResult) {
		super();
		this.targetImagePath = targetImagePath;
		this.templateImagePath = templateImagePath;
		this.outputImagePath = outputImagePath;
		this.maxMatchingPosition = maxMatchingPosition;
		this.matchingRate = matchingRate;
		this.minMaxLocResult = minMaxLocResult;
	}

	public MinMaxLocResult getMinMaxLocResult() {
		return minMaxLocResult;
	}

	public void setMinMaxLocResult(MinMaxLocResult minMaxLocResult) {
		this.minMaxLocResult = minMaxLocResult;
	}

	public String getTargetImagePath() {
		return targetImagePath;
	}

	public void setTargetImagePath(String targetImagePath) {
		this.targetImagePath = targetImagePath;
	}

	public String getTemplateImagePath() {
		return templateImagePath;
	}

	public void setTemplateImagePath(String templateImagePath) {
		this.templateImagePath = templateImagePath;
	}

	public String getOutputImagePath() {
		return outputImagePath;
	}

	public void setOutputImagePath(String outputImagePath) {
		this.outputImagePath = outputImagePath;
	}

	public double getMatchingRate() {
		return matchingRate;
	}

	public void setMatchingRate(double matchingRate) {
		this.matchingRate = matchingRate;
	}

	public Point getMaxMatchingPosition() {
		return maxMatchingPosition;
	}

	public void setMaxMatchingPosition(Point maxMatchingPosition) {
		this.maxMatchingPosition = maxMatchingPosition;
	}
}
