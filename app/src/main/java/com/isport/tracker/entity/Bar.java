package com.isport.tracker.entity;

import android.graphics.Path;
import android.graphics.Region;

public class Bar {
	private Boolean issettextvalues = Boolean.TRUE;
	private Boolean isDrawLine = Boolean.TRUE;
	private int mColor;
	private String mName = null;
	private float mValue;
	private String mValueString = null;
	private Path mPath = null;
	private Region mRegion = null;
	private String mName2 = null;
	private boolean isExercise;
	
	
	public boolean isExercise() {
		return isExercise;
	}
	public void setExercise(boolean isExercise) {
		this.isExercise = isExercise;
	}
	public String getName2() {
		return mName2;
	}
	public void setName2(String name2) {
		this.mName2 = name2;
	}
	public int getColor() {
		return mColor;
	}
	public void setColor(int color) {
		this.mColor = color;
	}
	public String getName() {
		return mName;
	}
	public void setName(String name) {
		this.mName = name;
	}
	public float getValue() {
		return mValue;
	}
	public void setValue(float value) {
		this.mValue = value;
	}
	
	public String getValueString()
	{
		if (mValueString != null) {
			return mValueString;
		} else {
			return String.valueOf(mValue);
		}
	}
	
	public void setValueString(final String valueString)
	{
		
		mValueString = valueString;
	}
	public void setSetvaluetextenble(Boolean i)
	{
		 issettextvalues = i;
		
	}
	public Boolean isSetvaluetext()
	{
		return issettextvalues;
		
	}
	
	public Path getPath() {
		return mPath;
	}
	public void setPath(Path path) {
		this.mPath = path;
	}
	public Region getRegion() {
		return mRegion;
	}
	public void setRegion(Region region) {
		this.mRegion = region;
	}
	public Boolean isLine(float lable,Boolean bool)
	{
		isDrawLine = bool;
		return isDrawLine;
	}
	public void setLine(float lable)
	{
	
	}
	
}
