package fractalzoomer.core.mpir;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

public class mpf_t extends Pointer {
    /** The size, in bytes, of the native structure. */
    public static final int SIZE;

    static {
        SIZE = Platform.isWindows() ? Native.SIZE_T_SIZE == 8 ? 20 : 16 : Native.SIZE_T_SIZE == 8 ? 24 : 16;
    }

    /**
     * Constructs an mpfr_t from a native address.
     *
     * @param peer the address of a block of native memory at least {@link #SIZE} bytes large
     */
    public mpf_t(long peer) {
        super(peer);
    }

    /**
     * Constructs an mpfr_t from a Pointer.
     *
     * @param from an block of native memory at least {@link #SIZE} bytes large
     */
    public mpf_t(Pointer from) {
        this(Pointer.nativeValue(from));
    }
}
