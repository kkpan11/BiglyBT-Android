package divstar.ico4a.io;

import androidx.annotation.NonNull;

import java.io.*;

public class CountingInputStream extends FilterInputStream {

	private int count;
	
	public CountingInputStream(InputStream src) {
		super(src);
	}
	
	public int getCount() {
		return count;
	}
	
	@Override
	public int read() throws IOException {
		int b = super.read();
		if (b != -1) {
			count++;
		}
		return b;
	}
	
	@Override
	public int read(@NonNull byte[] b, int off, int len) throws IOException {
		int r = super.read(b, off, len);
		if (r > 0) {
			count += r;
		}
		return r;
	}
}
