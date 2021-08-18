package gov.alaska.gmchandheld;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ImageFileRequestBody extends RequestBody {
    protected RequestBody delegate;
    public ImageFileRequestBody(RequestBody delegate) {
        this.delegate = delegate;
    }

    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return delegate.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        delegate.writeTo(sink);
        sink.flush();
    }
}
