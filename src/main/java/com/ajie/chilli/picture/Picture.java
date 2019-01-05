package com.ajie.chilli.picture;

import java.io.InputStream;

public interface Picture {

	String getName();

	String getAddress();

	double getSize();

	String getPath();

	String getType();

	InputStream getInputStream();

}
