//========================================================================
//Copyright 2007-2009 David Yu dyuproject@gmail.com
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package com.dyuproject.protostuff;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * A protobuf output where the writing of bytes is deferred (buffered in memory).
 * 
 * Once the message serialization is done, data can then be streamed to 
 * an {@link OutputStream} or a {@link ByteBuffer}.
 *
 * @author David Yu
 * @created Nov 9, 2009
 */
public final class DeferredOutput implements Output
{

    private ByteArrayNode root, current;
    private int size = 0;
    private final boolean forceWritePrimitives;

    public DeferredOutput(boolean forceWritePrimitives)
    {
        this.forceWritePrimitives = forceWritePrimitives;
    }
    
    /**
     * Gets the current size of this output.
     */
    public int getSize()
    {
        return size;
    }
    
    /**
     * Writes the raw bytes into the {@link OutputStream}.
     */
    public void streamTo(OutputStream out) throws IOException
    {
        for(ByteArrayNode node = root; node!=null; node = node.next)
        {
            byte[] bytes = node.bytes;
            out.write(bytes, 0, bytes.length);
        }
    }
    
    /**
     * Writes the raw bytes into the {@link ByteBuffer}.
     */
    public void streamTo(ByteBuffer buffer)
    {
        if(size>buffer.capacity())
        {
            throw new IllegalArgumentException("DeferredOutput size: " + size + 
                    " is greater than the provided buffer capacity: " + buffer.capacity());
        }
        
        for(ByteArrayNode node = root; node!=null; node = node.next)
            buffer.put(node.bytes);
    }
    
    /**
     * Returns the data written to this output as a single byte array.
     */
    public byte[] toByteArray()
    {
        int start = 0;
        byte[] buffer = new byte[size];        
        for(ByteArrayNode node = root; node!=null; node = node.next)
        {
            byte[] bytes = node.bytes;
            System.arraycopy(bytes, 0, buffer, start, bytes.length);
            start += bytes.length;
        }
        return buffer;
    }
    
    /**
     * Returns an iterator for the binary values of the serialized message.
     */
    public Iterator<byte[]> iterator()
    {
        return new Iterator<byte[]>()
        {
            ByteArrayNode node = root;
            
            public boolean hasNext()
            {
                return node.next!=null;
            }
            public byte[] next()
            {
                ByteArrayNode next = node;
                node = node.next;
                return next.bytes;
            }
            public void remove()
            {                
                throw new UnsupportedOperationException();
            }            
        };
    }

    public void writeInt32(int fieldNumber, int value) throws IOException
    {
        if(value==0 && !forceWritePrimitives)
            return;
        
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
        byte[] bytes = value<0 ? CodedOutput.getTagAndRawVarInt64Bytes(tag, value) : 
            CodedOutput.getTagAndRawVarInt32Bytes(tag, value);
        size += bytes.length;
        current = root==null ? (root=new ByteArrayNode(bytes)) : new ByteArrayNode(bytes, current);
    }
    
    public void writeUInt32(int fieldNumber, int value) throws IOException
    {
        if(value==0 && !forceWritePrimitives)
            return;
        
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
        byte[] bytes = CodedOutput.getTagAndRawVarInt32Bytes(tag, value);
        size += bytes.length;
        current = root==null ? (root=new ByteArrayNode(bytes)) : new ByteArrayNode(bytes, current);
    }
    
    public void writeSInt32(int fieldNumber, int value) throws IOException
    {
        if(value==0 && !forceWritePrimitives)
            return;
        
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
        byte[] bytes = CodedOutput.getTagAndRawVarInt32Bytes(tag, CodedOutput.encodeZigZag32(value));
        size += bytes.length;
        current = root==null ? (root=new ByteArrayNode(bytes)) : new ByteArrayNode(bytes, current);
    }
    
    public void writeFixed32(int fieldNumber, int value) throws IOException
    {
        if(value==0 && !forceWritePrimitives)
            return;
        
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_FIXED32);
        byte[] bytes = CodedOutput.getTagAndRawLittleEndian32Bytes(tag, value);
        size += bytes.length;
        current = root==null ? (root=new ByteArrayNode(bytes)) : new ByteArrayNode(bytes, current);
    }
    
    public void writeSFixed32(int fieldNumber, int value) throws IOException
    {
        if(value==0 && !forceWritePrimitives)
            return;
        
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_FIXED32);
        byte[] bytes = CodedOutput.getTagAndRawLittleEndian32Bytes(tag, value);
        size += bytes.length;
        current = root==null ? (root=new ByteArrayNode(bytes)) : new ByteArrayNode(bytes, current);
    }

    public void writeInt64(int fieldNumber, long value) throws IOException
    {
        if(value==0 && !forceWritePrimitives)
            return;
        
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
        byte[] bytes = CodedOutput.getTagAndRawVarInt64Bytes(tag, value);
        size += bytes.length;
        current = root==null ? (root=new ByteArrayNode(bytes)) : new ByteArrayNode(bytes, current);
    }
    
    public void writeUInt64(int fieldNumber, long value) throws IOException
    {
        if(value==0 && !forceWritePrimitives)
            return;
        
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
        byte[] bytes = CodedOutput.getTagAndRawVarInt64Bytes(tag, value);
        size += bytes.length;
        current = root==null ? (root=new ByteArrayNode(bytes)) : new ByteArrayNode(bytes, current);
    }
    
    public void writeSInt64(int fieldNumber, long value) throws IOException
    {
        if(value==0 && !forceWritePrimitives)
            return;
        
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
        byte[] bytes = CodedOutput.getTagAndRawVarInt64Bytes(tag, value);
        size += bytes.length;
        current = root==null ? (root=new ByteArrayNode(bytes)) : new ByteArrayNode(bytes, current);
    }
    
    public void writeFixed64(int fieldNumber, long value) throws IOException
    {
        if(value==0 && !forceWritePrimitives)
            return;
        
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_FIXED64);
        byte[] bytes = CodedOutput.getTagAndRawLittleEndian64Bytes(tag, value);
        size += bytes.length;
        current = root==null ? (root=new ByteArrayNode(bytes)) : new ByteArrayNode(bytes, current);
    }
    
    public void writeSFixed64(int fieldNumber, long value) throws IOException
    {
        if(value==0 && !forceWritePrimitives)
            return;
        
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_FIXED64);
        byte[] bytes = CodedOutput.getTagAndRawLittleEndian64Bytes(tag, value);
        size += bytes.length;
        current = root==null ? (root=new ByteArrayNode(bytes)) : new ByteArrayNode(bytes, current);
    }

    public void writeFloat(int fieldNumber, float value) throws IOException
    {
        if(value==0 && !forceWritePrimitives)
            return;
        
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_FIXED32);
        byte[] bytes = CodedOutput.getTagAndRawLittleEndian32Bytes(tag, Float.floatToRawIntBits(value));
        size += bytes.length;
        current = root==null ? (root=new ByteArrayNode(bytes)) : new ByteArrayNode(bytes, current);
    }

    public void writeDouble(int fieldNumber, double value) throws IOException
    {
        if(value==0 && !forceWritePrimitives)
            return;
        
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_FIXED64);
        byte[] bytes = CodedOutput.getTagAndRawLittleEndian64Bytes(tag, Double.doubleToRawLongBits(value));
        size += bytes.length;
        current = root==null ? (root=new ByteArrayNode(bytes)) : new ByteArrayNode(bytes, current);
    }

    public void writeBool(int fieldNumber, boolean value) throws IOException
    {
        if(!value && !forceWritePrimitives)
            return;
        
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
        byte[] bytes = CodedOutput.getTagAndRawVarInt32Bytes(tag, value ? 1 : 0);
        size += bytes.length;
        current = root==null ? (root=new ByteArrayNode(bytes)) : new ByteArrayNode(bytes, current);
    }

    public void writeEnum(int fieldNumber, int number) throws IOException
    {
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_VARINT);
        byte[] bytes = CodedOutput.getTagAndRawVarInt32Bytes(tag, number);
        size += bytes.length;
        current = root==null ? (root=new ByteArrayNode(bytes)) : new ByteArrayNode(bytes, current);
    }

    public void writeString(int fieldNumber, String value) throws IOException
    {
        if(value==null)
            return;

        byte[] bytes = value.getBytes(ByteString.UTF8);
        
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
        byte[] delimited = CodedOutput.getTagAndRawVarInt32Bytes(tag, bytes.length);
        size += delimited.length + bytes.length;
        
        if(root==null)
            current = new ByteArrayNode(bytes, (root=new ByteArrayNode(delimited)));
        else
            current = new ByteArrayNode(bytes, new ByteArrayNode(delimited, current));
    }

    public void writeBytes(int fieldNumber, ByteString value) throws IOException
    {
        if(value==null)
            return;

        byte[] bytes = value.getBytes();
        
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
        byte[] delimited = CodedOutput.getTagAndRawVarInt32Bytes(tag, bytes.length);
        size += delimited.length + bytes.length;
        
        if(root==null)
            current = new ByteArrayNode(bytes, (root=new ByteArrayNode(delimited)));
        else
            current = new ByteArrayNode(bytes, new ByteArrayNode(delimited, current));
    }
    
    public void writeByteArray(int fieldNumber, byte[] bytes) throws IOException
    {
        if(bytes==null)
            return;
        
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
        byte[] delimited = CodedOutput.getTagAndRawVarInt32Bytes(tag, bytes.length);
        size += delimited.length + bytes.length;
        
        if(root==null)
            current = new ByteArrayNode(bytes, (root=new ByteArrayNode(delimited)));
        else
            current = new ByteArrayNode(bytes, new ByteArrayNode(delimited, current));
    }

    public <T extends Message<T>> void writeMessage(int fieldNumber, T value) throws IOException
    {
        if(value==null)
            return;
        
        Schema<T> schema = value.cachedSchema();
        DeferredOutput output = new DeferredOutput(forceWritePrimitives);
        schema.writeTo(output, value);
        
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
        byte[] delimited = CodedOutput.getTagAndRawVarInt32Bytes(tag, output.size);
        size += delimited.length + output.size;
        
        if(root==null)
        {
            ByteArrayNode node = new ByteArrayNode(delimited);
            node.next = output.root;
            root = node;
            current = output.current;
        }
        else
        {
            new ByteArrayNode(delimited, current).next = output.root;
            current = output.current;
        }
    }
    
    public <T> void writeObject(int fieldNumber, T value, Schema<T> schema) throws IOException
    {
        if(value==null)
            return;
        
        DeferredOutput output = new DeferredOutput(forceWritePrimitives);
        schema.writeTo(output, value);
        
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
        byte[] delimited = CodedOutput.getTagAndRawVarInt32Bytes(tag, output.size);
        size += delimited.length + output.size;
        
        if(root==null)
        {
            ByteArrayNode node = new ByteArrayNode(delimited);
            node.next = output.root;
            root = node;
            current = output.current;
        }
        else
        {
            new ByteArrayNode(delimited, current).next = output.root;
            current = output.current;
        }
    }

    public <T> void writeObject(int fieldNumber, T value, Class<T> typeClass) throws IOException
    {
        if(value==null)
            return;

        ByteArrayOutputStream baos = new ByteArrayOutputStream(CodedOutput.DEFAULT_BUFFER_SIZE);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(value);
        byte[] bytes = baos.toByteArray();
        
        int tag = WireFormat.makeTag(fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED);
        byte[] delimited = CodedOutput.getTagAndRawVarInt32Bytes(tag, bytes.length);
        size += delimited.length + bytes.length;
        
        if(root==null)
            current = new ByteArrayNode(bytes, (root=new ByteArrayNode(delimited)));
        else
            current = new ByteArrayNode(bytes, new ByteArrayNode(delimited, current));
    }

}
