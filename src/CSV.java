import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSV {
    static final private int NUMMARK = 10;
    static final private char COMMA = ',';
    static final private char DQUOTE = '"';
    static final private char CRETURN = '\r';
    static final private char LFEED = '\n';
    static final private char SQUOTE = '\'';
    static final private char COMMENT = '#';

    private boolean stripMultipleNewLines;

    private char separator;
    private ArrayList<String> fields;
    private boolean endOfFileSeen;
    private Reader in;

    static public Reader stripBom(InputStream in) throws IOException, UnsupportedEncodingException {
        PushbackInputStream pushbackInputStream = new PushbackInputStream(in, 3);
        byte[] bytes = new byte[3];
        int len = pushbackInputStream.read(bytes, 0, bytes.length);
        if ( (bytes[0] & 255) == 239 && len == 3) {
            if ( (bytes[1] & 255) == 187 && (bytes[2] & 255) == 191) {
                return new InputStreamReader(pushbackInputStream, "UTF-8");
            }
            else {
                pushbackInputStream.unread(bytes, 0, len);
            }
        }
        else if (len >= 2) {
            if ( (bytes[0] & 0xFF) == 0xFE && (bytes[1] & 0xFF) == 0xFF ) {
                return new InputStreamReader(pushbackInputStream, "UTF-16BE");
            }
            else if ( (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xFE ) {
                return new InputStreamReader(pushbackInputStream, "UTF-16LE");
            }
            else {
                pushbackInputStream.unread(bytes, 0, len);
            }
        }
        else if (len > 0) {
            pushbackInputStream.unread(bytes, 0, len);
        }
        return new InputStreamReader(pushbackInputStream, "UTF-8");
    }

    public CSV(boolean stripMultipleNewLines, char separator, Reader input) {
        this.stripMultipleNewLines = stripMultipleNewLines;
        this.separator = separator;
        this.fields = new ArrayList<String>();
        this.endOfFileSeen = false;
        this.in = new BufferedReader(input);
    }

    public CSV(boolean stripMultipleNewLines, char separator, InputStream input) throws IOException, UnsupportedEncodingException {
        this.stripMultipleNewLines = stripMultipleNewLines;
        this.separator = separator;
        this.fields = new ArrayList<String>();
        this.endOfFileSeen = false;
        this.in = new BufferedReader(stripBom(input));
    }

    public boolean hasNext() throws IOException {
        if (endOfFileSeen) {
            return false;
        }
        fields.clear();
        endOfFileSeen = split(in, fields);
        if (endOfFileSeen) {
            return !fields.isEmpty();
        }
        else {
            return true;
        }
    }

    public List<String> next()
    {
        return fields;
    }

    // Returns true if EOF seen.
    static private boolean discardLinefeed(Reader in, boolean stripMultiple) throws java.io.IOException
    {
        if ( stripMultiple ) {
            in.mark(NUMMARK);
            int value = in.read();
            while ( value != -1 ) {
                char c = (char)value;
                if ( c != CRETURN && c != LFEED ) {
                    in.reset();
                    return false;
                } else {
                    in.mark(NUMMARK);
                    value = in.read();
                }
            }
            return true;
        } else {
            in.mark(NUMMARK);
            int value = in.read();
            if ( value == -1 ) return true;
            else if ( (char)value != LFEED ) in.reset();
            return false;
        }
    }

    private boolean skipComment(Reader in) throws java.io.IOException {
    /* Discard line. */
        int value;
        while ( (value = in.read()) != -1 ) {
            char c = (char)value;
            if ( c == CRETURN )
                return discardLinefeed( in, stripMultipleNewLines );
        }
        return true;
    }

    // Returns true when EOF has been seen.
    private boolean split(Reader in,ArrayList<String> fields) throws java.io.IOException {
        StringBuilder sbuf = new StringBuilder();
        int value;
        while ( (value = in.read()) != -1 ) {
            char c = (char)value;
            switch(c) {
                case CRETURN:
                    if ( sbuf.length() > 0 ) {
                        fields.add( sbuf.toString() );
                        sbuf.delete( 0, sbuf.length() );
                    }
                    return discardLinefeed( in, stripMultipleNewLines );

                case LFEED:
                    if ( sbuf.length() > 0 ) {
                        fields.add( sbuf.toString() );
                        sbuf.delete( 0, sbuf.length() );
                    }
                    if (stripMultipleNewLines) {
                        return discardLinefeed( in, stripMultipleNewLines);
                    }
                    else return false;

                case DQUOTE: {
                    while ( (value = in.read()) != -1 ) {
                        c = (char)value;
                        if ( c == DQUOTE ) {
                            in.mark(NUMMARK);
                            if ( (value = in.read()) == -1 ) {
                                if ( sbuf.length() > 0 ) {
                                    fields.add( sbuf.toString() );
                                    sbuf.delete( 0, sbuf.length() );
                                }
                                return true;
                            } else if ( (c = (char)value) == DQUOTE ) {
                                sbuf.append( DQUOTE );
                            } else if ( c == CRETURN ) {
                                if ( sbuf.length() > 0 ) {
                                    fields.add( sbuf.toString() );
                                    sbuf.delete( 0, sbuf.length() );
                                }
                                return discardLinefeed( in, stripMultipleNewLines );
                            } else if ( c == LFEED ) {
                                if ( sbuf.length() > 0 ) {
                                    fields.add( sbuf.toString() );
                                    sbuf.delete( 0, sbuf.length() );
                                }
                                // No need to check further. At this
                                // point, we have not yet hit EOF, so
                                // we return false.
                                if (stripMultipleNewLines )
                                    return discardLinefeed( in, stripMultipleNewLines );
                                else return false;
                            } else {
                                in.reset();
                                break;
                            }
                        } else {
                            sbuf.append( c );
                        }
                    }
                    if ( value == -1 ) {
                        if ( sbuf.length() > 0 ) {
                            fields.add( sbuf.toString() );
                            sbuf.delete( 0, sbuf.length() );
                        }
                        return true;
                    }
                }
                break;

                default:
                    if ( c == separator ) {
                        fields.add( sbuf.toString() );
                        sbuf.delete(0, sbuf.length());
                    } else {
          /* A comment line is a line starting with '#' with
           * optional whitespace at the start. */
                        if ( c == COMMENT && fields.isEmpty() &&
                                sbuf.toString().trim().isEmpty() ) {
                            boolean eof = skipComment(in);
                            if ( eof ) return eof;
                            else sbuf.delete(0, sbuf.length());
            /* Continue with next line if not eof. */
                        } else sbuf.append(c);
                    }
            }
        }
        if ( sbuf.length() > 0 ) {
            fields.add( sbuf.toString() );
            sbuf.delete( 0, sbuf.length() );
        }
        return true;
    }

}
