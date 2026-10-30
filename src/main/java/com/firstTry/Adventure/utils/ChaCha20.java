package com.firstTry.Adventure.utils;

/**
 * 纯Java实现的 ChaCha20 流密码（IETF变体：32位计数器 + 96位nonce + 20轮）。
 *
 * 仅用于隐藏房间图片落库：前端 colourEgg.js 对图片分片做了 ChaCha20 加密，本类用于在
 * 服务端把收到的加密分片解密还原成真实图片内容（不是加密用途），算法需与前端严格保持一致。
 */
public class ChaCha20 {

	private static final int[] CONST = { 0x61707865, 0x3320646e, 0x79622d32, 0x6b206574 };

	private ChaCha20() {
	}

	/** 小端读取4字节为一个32位整数 */
	private static int le32(byte[] b, int off) {
		return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8) | ((b[off + 2] & 0xFF) << 16)
				| ((b[off + 3] & 0xFF) << 24);
	}

	/** 一轮 quarter round，对应前端 _chachaQr */
	private static void quarterRound(int[] x, int a, int b, int c, int d) {
		x[a] += x[b]; x[d] = Integer.rotateLeft(x[d] ^ x[a], 16);
		x[c] += x[d]; x[b] = Integer.rotateLeft(x[b] ^ x[c], 12);
		x[a] += x[b]; x[d] = Integer.rotateLeft(x[d] ^ x[a], 8);
		x[c] += x[d]; x[b] = Integer.rotateLeft(x[b] ^ x[c], 7);
	}

	/** 生成一个64字节keystream块，对应前端 _chachaBlock */
	private static void block(byte[] key, byte[] nonce, int counter, byte[] out, int outOff) {
		int[] state = new int[16];
		state[0] = CONST[0]; state[1] = CONST[1]; state[2] = CONST[2]; state[3] = CONST[3];
		for (int i = 0; i < 8; i++) {
			state[4 + i] = le32(key, i * 4);
		}
		state[12] = counter;
		state[13] = le32(nonce, 0);
		state[14] = le32(nonce, 4);
		state[15] = le32(nonce, 8);
		int[] x = state.clone();
		for (int round = 0; round < 10; round++) {
			quarterRound(x, 0, 4, 8, 12);
			quarterRound(x, 1, 5, 9, 13);
			quarterRound(x, 2, 6, 10, 14);
			quarterRound(x, 3, 7, 11, 15);
			quarterRound(x, 0, 5, 10, 15);
			quarterRound(x, 1, 6, 11, 12);
			quarterRound(x, 2, 7, 8, 13);
			quarterRound(x, 3, 4, 9, 14);
		}
		for (int i = 0; i < 16; i++) {
			int v = x[i] + state[i];
			out[outOff + i * 4] = (byte) v;
			out[outOff + i * 4 + 1] = (byte) (v >>> 8);
			out[outOff + i * 4 + 2] = (byte) (v >>> 16);
			out[outOff + i * 4 + 3] = (byte) (v >>> 24);
		}
	}

	/** 生成从字节偏移byteOffset起、长度length的keystream，对应前端 _chachaKeystream */
	public static byte[] keystream(byte[] key, byte[] nonce, long byteOffset, int length) {
		byte[] out = new byte[length];
		long blockIdx = byteOffset / 64;
		int inOffset = (int) (byteOffset % 64);
		byte[] blk = new byte[64];
		int written = 0;
		while (written < length) {
			block(key, nonce, (int) blockIdx, blk, 0);
			int take = Math.min(64 - inOffset, length - written);
			System.arraycopy(blk, inOffset, out, written, take);
			written += take;
			blockIdx++;
			inOffset = 0;
		}
		return out;
	}

	/** 对一段数据做加密/解密（异或同一段keystream），byteOffset为该段在整段字节流中的偏移 */
	public static byte[] crypt(byte[] data, byte[] key, byte[] nonce, long byteOffset) {
		byte[] ks = keystream(key, nonce, byteOffset, data.length);
		byte[] out = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
			out[i] = (byte) (data[i] ^ ks[i]);
		}
		return out;
	}
}