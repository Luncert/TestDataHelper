package org.luncert.testdatahelper.component;

import javax.websocket.OnError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class DualChannelStream {

  /**
   * The maximum size of array to allocate.
   * Some VMs reserve some header words in an array.
   * Attempts to allocate larger arrays may result in
   * OutOfMemoryError: Requested array size exceeds VM limit
   */
  private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

  private boolean blocking;
  private volatile boolean closed;
  private int[] buf;
  private int writePos;
  private int readPos;

  private OutputStream writeStream;
  private InputStream readStream;

  public DualChannelStream() {
    this(32, false);
  }

  /**
   * Create dual channel stream.
   * @param size initial buffer size
   * @param blocking if true, block read operation when writing
   */
  public DualChannelStream(int size, boolean blocking) {
    buf = new int[size];
    this.blocking = blocking;
    writeStream = new WriteStream();
    readStream = new ReadStream();
  }

  /**
   * Increases the capacity if necessary to ensure that it can hold
   * at least the number of elements specified by the minimum
   * capacity argument.
   *
   * @param  minCapacity the desired minimum capacity
   * @throws OutOfMemoryError if {@code minCapacity < 0}.  This is
   * interpreted as a request for the unsatisfiable large capacity
   * {@code (long) Integer.MAX_VALUE + (minCapacity - Integer.MAX_VALUE)}.
   */
  private void ensureCapacity(int minCapacity) {
    // overflow-conscious code
    if (minCapacity - buf.length > 0) {
      grow(minCapacity);
    }
  }

  /**
   * Increases the capacity to ensure that it can hold at least the
   * number of elements specified by the minimum capacity argument.
   *
   * @param minCapacity the desired minimum capacity
   */
  private void grow(int minCapacity) {
    // overflow-conscious code
    int oldCapacity = buf.length;
    int newCapacity = oldCapacity << 1;
    if (newCapacity - minCapacity < 0) {
      newCapacity = minCapacity;
    }
    if (newCapacity - MAX_ARRAY_SIZE > 0) {
      newCapacity = hugeCapacity(minCapacity);
    }
    buf = Arrays.copyOf(buf, newCapacity);
  }

  private static int hugeCapacity(int minCapacity) {
    if (minCapacity < 0) {
      // overflow
      throw new OutOfMemoryError();
    }
    return (minCapacity > MAX_ARRAY_SIZE) ?
        Integer.MAX_VALUE :
        MAX_ARRAY_SIZE;
  }

  /**
   * Get write stream.
   * @return OutputStream.
   */
  public OutputStream getWriteStream() {
    return writeStream;
  }

  private class WriteStream extends OutputStream {

    @Override
    public void write(int b) throws IOException {
      synchronized (this) {
        if (DualChannelStream.this.closed) {
          throw new IOException("DualChannelStream has been closed.");
        }
        ensureCapacity(writePos + 1);
        buf[writePos] = b;
        writePos += 1;
        if (DualChannelStream.this.blocking) {
          synchronized (DualChannelStream.this) {
            DualChannelStream.this.notify();
          }
        }
      }
    }

    @Override
    public void close() {
      DualChannelStream.this.close();
    }
  }

  /**
   * Get read stream.
   * @return InputStream
   */
  public InputStream getReadStream() {
    return readStream;
  }

  private class ReadStream extends InputStream {

    @Override
    public int read() throws IOException {
      synchronized (this) {
        if (readPos < writePos) {
          return buf[readPos++] & 0xff;
        }
        if (!DualChannelStream.this.closed) {
          if (DualChannelStream.this.blocking) {
            try {
              synchronized (DualChannelStream.this) {
                DualChannelStream.this.wait();
              }
            } catch (InterruptedException e) {
              throw new IOException(e);
            }
            if (readPos < writePos) {
              return buf[readPos++] & 0xff;
            }
          }
        }
        return -1;
      }
    }

    @Override
    public void close() {
      DualChannelStream.this.close();
    }
  }

  public void close() {
    if (!closed) {
      synchronized (this) {
        closed = true;
        notifyAll();
      }
    }
  }

  public boolean allDataConsumed() {
    return readPos == writePos;
  }
}
