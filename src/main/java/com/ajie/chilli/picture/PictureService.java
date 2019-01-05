package com.ajie.chilli.picture;

import java.io.InputStream;

public interface PictureService {

	Picture create(InputStream stream);

	Picture create(Picture picture);

	Picture create(String path, InputStream stream);

	Picture get(String path);
}
