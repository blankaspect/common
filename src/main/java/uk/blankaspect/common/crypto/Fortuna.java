/*====================================================================*\

Fortuna.java

Fortuna pseudo-random number generator base class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.crypto;

//----------------------------------------------------------------------


// IMPORTS


import java.io.UnsupportedEncodingException;

import uk.blankaspect.common.exception.UnexpectedRuntimeException;

//----------------------------------------------------------------------


// FORTUNA PSEUDO-RANDOM NUMBER GENERATOR BASE CLASS


/**
 * This is the base class for implementations of the Fortuna pseudo-random number generator (PRNG)
 * algorithm.  Subclasses are expected to provide a block cipher running in counter mode as the underlying
 * generator.
 * <p>
 * The Fortuna PRNG is described in Niels Ferguson and Bruce Schneier, <i>Practical cryptography</i> (Wiley,
 * 2003; ISBN 0471223573).
 * </p>
 */

public abstract class Fortuna
	implements Cloneable, IEntropyConsumer
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/**
	 * The number of entropy pools.
	 */
	public static final		int	NUM_ENTROPY_POOLS	= 32;

	/**
	 * The minimum number of bytes of random data that can be extracted from the PRNG in a single request.
	 */
	public static final		int	MIN_BLOCK_SIZE	= 1;

	/**
	 * The maximum number of bytes of random data that can be extracted from the PRNG in a single request.
	 */
	public static final		int	MAX_BLOCK_SIZE	= 1 << 20;

	/**
	 * The minumum number of bytes that the first entropy pool must contain to allow the PRNG to be
	 * reseeded.
	 */
	public static final		int	RESEED_ENTROPY_THRESHOLD	= 64;

	private static final	int	MIN_RESEED_INTERVAL	= 100;

	private static final	String	KEY_ENCODING_NAME	= "UTF-8";

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// EXCLUSIVE-OR COMBINER CLASS


	/**
	 * This class implements a means of combining a stream of byte data and random data generated by a PRNG
	 * with an exclusive-OR operation.
	 */

	public static class XorCombiner
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates an object that will combine data and random data generated by a specified PRNG with an
		 * exclusive-OR operation.
		 *
		 * @param  prng       the pseudo-random number generator that will generate the random data for the
		 *                    exclusive-OR operation.
		 * @param  blockSize  the number of bytes of random data that will be extracted from {@code prng}
		 *                    with each request.
		 * @throws IllegalArgumentException
		 *           if {@code blockSize} is less than 1 or greater than 2<sup>20</sup> (1048576).
		 */

		public XorCombiner(Fortuna prng,
						   int     blockSize)
		{
			if ((blockSize < MIN_BLOCK_SIZE) || (blockSize > MAX_BLOCK_SIZE))
				throw new IllegalArgumentException();

			int bufferSize = Integer.highestOneBit(blockSize);
			if (bufferSize < blockSize)
				bufferSize <<= 1;

			this.prng = prng;
			buffer = new byte[bufferSize];
			indexMask = bufferSize - 1;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns the PRNG of this object.
		 *
		 * @return the PRNG of this object.
		 */

		public Fortuna getPrng()
		{
			return prng;
		}

		//--------------------------------------------------------------

		/**
		 * Combines some specified data and random data generated by this object's PRNG with an exclusive-OR
		 * operation.
		 * <p>
		 * The exclusive-OR operation is applied to {@code data} in place.
		 * <p>
		 *
		 * @param  data  the data to which the exclusive-OR operation will be applied.
		 * @throws IllegalArgumentException
		 *           if {@code data} is {@code null}.
		 */

		public void combine(byte[] data)
		{
			combine(data, 0, data.length);
		}

		//--------------------------------------------------------------

		/**
		 * Combines a specified number of bytes of data and random data generated by this object's PRNG with
		 * an exclusive-OR operation.
		 * <p>
		 * The exclusive-OR operation is applied to {@code data} in place.
		 * <p>
		 *
		 * @param  data    the data to which the exclusive-OR operation will be applied.
		 * @param  offset  the start offset of the data in {@code data}.
		 * @param  length  the number of bytes to which the exclusive-OR operation will be applied.
		 * @throws IllegalArgumentException
		 *           if
		 *           <ul>
		 *             <li>{@code data} is {@code null}, or</li>
		 *             <li>{@code length} is negative, or</li>
		 *             <li>{@code offset} + {@code length} is greater than the length of {@code data}.<li>
		 *           </ul>
		 * @throws IndexOutOfBoundsException
		 *           if {@code offset} is negative or greater than the length of {@code data}.
		 */

		public void combine(byte[] data,
							int    offset,
							int    length)
		{
			if (data == null)
				throw new IllegalArgumentException();
			if ((offset < 0) || (offset > data.length))
				throw new IndexOutOfBoundsException();
			if ((length < 0) || (length > data.length - offset))
				throw new IllegalArgumentException();

			int endOffset = offset + length;
			for (int i = offset; i < endOffset; i++)
			{
				if (index == 0)
					prng.getRandomBytes(buffer);
				data[i] ^= buffer[index++];
				index &= indexMask;
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	Fortuna	prng;
		private	byte[]	buffer;
		private	int		index;
		private	int		indexMask;

	}

	//==================================================================


	// ENTROPY POOL CLASS


	/**
	 * This class implements an entropy pool for the Fortuna PRNG.  It uses the double-iteration SHA-256
	 * hash function (SHAd-256) to compress the stored entropy.  In the implementation of SHAd-256, the
	 * underlying SHA-256 hash function is provided by a {@link java.security.MessageDigest} object.
	 * Depending on its implementation, the {@link java.security.MessageDigest MessageDigest} object may or
	 * may not be cloneable.  The {@code EntropyPool} class handles both possibilities in order to be
	 * cloneable itself: if the hash-function ({@link java.security.MessageDigest MessageDigest}) object is
	 * cloneable, the entropy is stored in the state of the hash-function object and extracted on demand;
	 * otherwise, each time entropy is added to the pool, it is hashed with the existing entropy and
	 * immediately extracted from the hash-function object, which is less efficient.
	 */

	private static class EntropyPool
		implements Cloneable
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private EntropyPool()
		{
			hash = new ShaD256();
			cannotCloneHash = !hash.canClone();
			if (cannotCloneHash)
				pool = hash.digest();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a copy of this entropy pool.
		 *
		 * @return a copy of this entropy pool.
		 */

		@Override
		public EntropyPool clone()
		{
			try
			{
				EntropyPool copy = (EntropyPool)super.clone();

				if (cannotCloneHash)
				{
					copy.hash = new ShaD256();
					copy.pool = pool.clone();
				}
				else
					copy.hash = hash.clone();

				return copy;
			}
			catch (CloneNotSupportedException e)
			{
				throw new UnexpectedRuntimeException(e);
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Resets this entropy pool.
		 */

		private synchronized void reset()
		{
			length = 0;
			hash.reset();
			if (cannotCloneHash)
				pool = hash.digest();
		}

		//--------------------------------------------------------------

		/**
		 * Adds a specified number of bytes of random data to this entropy pool.
		 *
		 * @param data    the random data that will be added to the entropy pool.
		 * @param offset  the offset of the start of the random data in {@code data}.
		 * @param length  the number of bytes of random data to add.
		 */

		private synchronized void add(byte[] data,
									  int    offset,
									  int    length)
		{
			if (cannotCloneHash)
			{
				hash.update(pool);
				hash.update(data, offset, length);
				pool = hash.digest();
			}
			else
				hash.update(data, offset, length);
			this.length += length;
		}

		//--------------------------------------------------------------

		/**
		 * Removes the entropy from this pool in the form of a SHAd-256 hash value of the pool's contents.
		 *
		 * @return the entropy from this pool in the form of a SHAd-256 hash value of the pool's contents.
		 */

		private synchronized byte[] remove()
		{
			length = 0;
			return (cannotCloneHash ? pool : hash.digest());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	int		length;
		private	ShaD256	hash;
		private	byte[]	pool;
		private	boolean	cannotCloneHash;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a Fortuna pseudo-random number generator that is initialised with a random seed derived from
	 * the sources of entropy.
	 *
	 * @param keySize    the cipher's key size (in bytes).
	 * @param blockSize  the cipher's block size (in bytes).
	 */

	protected Fortuna(int keySize,
					  int blockSize)
	{
		this(keySize, blockSize, (byte[])null);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a Fortuna pseudo-random number generator that is initialised with a specified seed.
	 * <p>
	 * If the seed is {@code null}, a random seed derived from the sources of entropy will be used.  In this
	 * case, the PRNG will not be able to generate random data until sufficient entropy has accumulated for
	 * the generator to be reseeded.  The ability to reseed can be tested with {@link #canReseed()}.
	 * </p>
	 *
	 * @param keySize    the cipher's key size (in bytes).
	 * @param blockSize  the cipher's block size (in bytes).
	 * @param seed       a sequence of bytes that will be used to seed the pseudo-random number generator.
	 *                   If {@code seed} is {@code null}, a random seed derived from the sources of entropy
	 *                   will be used.
	 */

	protected Fortuna(int    keySize,
					  int    blockSize,
					  byte[] seed)
	{
		// Initialise instance fields
		this.keySize = keySize;
		hash = new ShaD256();
		blockBuffer = new byte[blockSize];
		entropyPools = new EntropyPool[NUM_ENTROPY_POOLS];
		for (int i = 0; i < entropyPools.length; i++)
			entropyPools[i] = new EntropyPool();

		// Initialise cipher
		initCipher();

		// Set key
		if (seed != null)
			setKey(hash.digest(seed));
	}

	//------------------------------------------------------------------

	/**
	 * Creates a Fortuna pseudo-random number generator that is initialised with a specified seed in the
	 * form of a string.
	 * <p>
	 * If the seed is {@code null}, a random seed derived from the sources of entropy will be used.  In this
	 * case, the PRNG will not be able to generate random data until sufficient entropy has accumulated for
	 * the generator to be reseeded.  The ability to reseed can be tested with {@link #canReseed()}.
	 * </p>
	 *
	 * @param keySize    the cipher's key size (in bytes).
	 * @param blockSize  the cipher's block size (in bytes).
	 * @param seed       a string whose UTF-8 encoding will be used to seed the pseudo-random number
	 *                   generator.  If {@code seed} is {@code null}, a random seed derived from the sources
	 *                   of entropy will be used.
	 */

	protected Fortuna(int    keySize,
					  int    blockSize,
					  String seed)
	{
		this(keySize, blockSize, (seed == null) ? (byte[])null : keyStringToBytes(seed));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Converts a string to an array of bytes with the UTF-8 encoding.
	 * <p>
	 * The method is intended to be used for converting a key in the form of a string to a byte array for
	 * use as the seed of a pseudo-random number generator.
	 * </p>
	 *
	 * @param  key  the string that will be converted.
	 * @return the UTF-8 encoding of {@code key}.
	 * @throws UnexpectedRuntimeException
	 *           if the UTF-8 character encoding is not supported by the Java implementation.
	 */

	public static byte[] keyStringToBytes(String key)
	{
		try
		{
			return key.getBytes(KEY_ENCODING_NAME);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new UnexpectedRuntimeException(e);
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Abstract methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Initialises this PRNG's cipher.
	 * <p>
	 * This method is called by the constructors of this class to give subclasses the opportunity to
	 * initialise the cipher before the PRNG's key is set.
	 * </p>
	 */

	protected abstract void initCipher();

	//------------------------------------------------------------------

	/**
	 * Resets this PRNG's cipher.
	 */

	protected abstract void resetCipher();

	//------------------------------------------------------------------

	/**
	 * Sets the encryption key of this PRNG's cipher.
	 *
	 * @param key  the key that will be set as the cipher's encryption key.
	 */

	protected abstract void setCipherKey(byte[] key);

	//------------------------------------------------------------------

	/**
	 * Increments the block counter of this PRNG's cipher.
	 */

	protected abstract void incrementCounter();

	//------------------------------------------------------------------

	/**
	 * Encrypts the value of the block counter with this PRNG's cipher and stores the result in a specified
	 * buffer.
	 *
	 * @param buffer  the buffer in which the encrypted data will be stored.
	 * @param offset  the offset in {@code buffer} at which the first byte of encrypted data will be stored.
	 */

	protected abstract void encryptCounter(byte[] buffer,
										   int    offset);

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IEntropyConsumer interface
////////////////////////////////////////////////////////////////////////

	/**
	 * Adds a specified byte of random data to the entropy pools of this PRNG.
	 *
	 * @param b  the byte of random data that will be added to the entropy pools.
	 */

	public void addRandomByte(byte b)
	{
		addRandomBytes(new byte[]{ b }, 0, 1);
	}

	//------------------------------------------------------------------

	/**
	 * Adds a specified number of bytes of random data to the entropy pools of this PRNG.
	 *
	 * @param data    the random data that will be added to the entropy pools.
	 * @param offset  the offset of the start of the random data in {@code data}.
	 * @param length  the number of bytes of random data to add.
	 */

	public void addRandomBytes(byte[] data,
							   int    offset,
							   int    length)
	{
		// Add data to entropy pool
		entropyPools[entropyPoolIndex].add(data, offset, length);

		// Increment entropy pool index
		if (++entropyPoolIndex >= NUM_ENTROPY_POOLS)
			entropyPoolIndex = 0;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a copy of this PRNG.
	 * <p>
	 * The copy is initially identical to this object (it has the same state and entropy pools), but the
	 * two objects are independent.
	 * </p>
	 *
	 * @return a copy of this PRNG.
	 */

	@Override
	public Fortuna clone()
	{
		try
		{
			Fortuna copy = (Fortuna)super.clone();

			copy.hash = new ShaD256();
			copy.key = key.clone();
			copy.blockBuffer = blockBuffer.clone();
			for (int i = 0; i < entropyPools.length; i++)
				copy.entropyPools[i] = entropyPools[i].clone();

			return copy;
		}
		catch (CloneNotSupportedException e)
		{
			throw new UnexpectedRuntimeException(e);
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the key size (in bytes) of this PRNG.
	 *
	 * @return the key size (in bytes) of this PRNG.
	 */

	public int getKeySize()
	{
		return keySize;
	}

	//------------------------------------------------------------------

	/**
	 * Initialises this PRNG with a random seed derived from the sources of entropy.
	 */

	public void init()
	{
		init((byte[])null);
	}

	//------------------------------------------------------------------

	/**
	 * Initialises this PRNG with a specified seed.
	 *
	 * @param seed  an array of bytes that will be used to seed the pseudo-random number generator.  If
	 *              {@code seed} is {@code null}, a random seed derived from the sources of entropy will be
	 *              used.
	 */

	public void init(byte[] seed)
	{
		// Reset generator state
		resetCipher();
		hash.reset();
		for (EntropyPool entropyPool : entropyPools)
			entropyPool.reset();
		entropyPoolIndex = 0;
		reseedIndex = 0;
		lastReseedTime = 0;

		// Set key
		if (seed == null)
			key = null;
		else
			setKey(hash.digest(seed));
	}

	//------------------------------------------------------------------

	/**
	 * Initialises this PRNG with a specified seed in the form of a string.
	 *
	 * @param seed  a string whose UTF-8 encoding will be used to seed the pseudo-random number generator.
	 */

	public void init(String seed)
	{
		init(keyStringToBytes(seed));
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this PRNG can generate random numbers.
	 * <p>
	 * The generator can generate random numbers if it has been seeded.
	 * </p>
	 *
	 * @return {@code true} if the generator can generate random numbers, {@code false} otherwise.
	 */

	public boolean canGenerate()
	{
		return (key != null);
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this PRNG can be reseeded.
	 * <p>
	 * The generator can be reseeded if the first entropy pool contains at least 64 bytes of entropy and at
	 * least 100 milliseconds have elapsed since the generator was last reseeded.
	 * </p>
	 *
	 * @return {@code true} if the generator can be reseeded, {@code false} otherwise.
	 */

	public boolean canReseed()
	{
		return ((entropyPools[0].length >= RESEED_ENTROPY_THRESHOLD) &&
				 (System.currentTimeMillis() >= lastReseedTime + MIN_RESEED_INTERVAL));
	}

	//------------------------------------------------------------------

	/**
	 * Returns the lengths of the entropy pools of this PRNG.
	 *
	 * @return an array containing the length (in bytes) of each of the 32 entropy pools.
	 */

	public int[] getEntropyPoolLengths()
	{
		int[] lengths = new int[NUM_ENTROPY_POOLS];
		for (int i = 0; i < lengths.length; i++)
			lengths[i] = entropyPools[i].length;
		return lengths;
	}

	//------------------------------------------------------------------

	/**
	 * Generates and returns a random byte.
	 *
	 * @return a random byte.
	 * @throws IllegalStateException
	 *           if the generator has not been seeded.
	 */

	public byte getRandomByte()
	{
		return getRandomBytes(1)[0];
	}

	//------------------------------------------------------------------

	/**
	 * Generates and returns a specified number of random bytes, which are stored in a buffer that is
	 * allocated by this method.
	 *
	 * @param  length  the number of bytes to generate.
	 * @return a buffer containing {@code length} random bytes.
	 * @throws IllegalArgumentException
	 *           if {@code length} is negative or greater than 2<sup>20</sup> (1048576).
	 * @throws IllegalStateException
	 *           if the generator has not been seeded.
	 */

	public byte[] getRandomBytes(int length)
	{
		if ((length < 0) || (length > MAX_BLOCK_SIZE))
			throw new IllegalArgumentException();

		byte[] buffer = new byte[length];
		getRandomBytes(buffer, 0, length);
		return buffer;
	}

	//------------------------------------------------------------------

	/**
	 * Generates random bytes and stores them in a specified buffer.  The number of bytes generated is equal
	 * to the length of the buffer.
	 *
	 * @param  buffer  the buffer in which the random data will be stored.
	 * @throws IllegalArgumentException
	 *           if {@code buffer} is {@code null} or the length of {@code buffer} is greater than
	 *           2<sup>20</sup> (1048576).
	 * @throws IllegalStateException
	 *           if the generator has not been seeded.
	 */

	public void getRandomBytes(byte[] buffer)
	{
		getRandomBytes(buffer, 0, buffer.length);
	}

	//------------------------------------------------------------------

	/**
	 * Generates a specified number of random bytes and stores them in a specified buffer.
	 *
	 * @param  buffer  the buffer in which the random data will be stored.
	 * @param  offset  the offset in {@code buffer} at which the first byte of random data will be stored.
	 * @param  length  the number of bytes to generate.
	 * @throws IllegalArgumentException
	 *           if
	 *           <ul>
	 *             <li>{@code buffer} is {@code null}, or</li>
	 *             <li>{@code length} is negative or greater than 2<sup>20</sup> (1048576), or</li>
	 *             <li>{@code offset} + {@code length} is greater than the length of {@code buffer}.</li>
	 *           </ul>
	 * @throws IndexOutOfBoundsException
	 *           if {@code offset} is negative or greater than the length of {@code buffer}.
	 * @throws IllegalStateException
	 *           if the generator has not been seeded.
	 */

	public void getRandomBytes(byte[] buffer,
							   int    offset,
							   int    length)
	{
		// Validate arguments
		if (buffer == null)
			throw new IllegalArgumentException();
		if ((offset < 0) || (offset > buffer.length))
			throw new IndexOutOfBoundsException();
		if ((length < 0) || (length > buffer.length - offset) || (length > MAX_BLOCK_SIZE))
			throw new IllegalArgumentException();

		// Test for reseed
		if (canReseed())
			reseed();

		// Test whether generator has been seeded
		if (key == null)
			throw new IllegalStateException();

		// Generate random data
		generateBlock(buffer, offset, length);

		// Set a new key
		generateBlock(key, 0, keySize);
		setKey(key);
	}

	//------------------------------------------------------------------

	/**
	 * Returns a randomly generated 32-bit integer.
	 *
	 * @return a randomly generated 32-bit integer.
	 * @throws IllegalStateException
	 *           if the generator has not been seeded.
	 */

	public int getRandomInt()
	{
		byte[] data = getRandomBytes(Integer.SIZE / Byte.SIZE);
		int value = 0;
		for (int i = 0; i < data.length; i++)
		{
			value <<= 8;
			value |= data[i] & 0xFF;
		}
		return value;
	}

	//------------------------------------------------------------------

	/**
	 * Returns a randomly generated 64-bit integer.
	 *
	 * @return a randomly generated 64-bit integer.
	 * @throws IllegalStateException
	 *           if the generator has not been seeded.
	 */

	public long getRandomLong()
	{
		byte[] data = getRandomBytes(Long.SIZE / Byte.SIZE);
		long value = 0;
		for (int i = 0; i < data.length; i++)
		{
			value <<= 8;
			value |= data[i] & 0xFF;
		}
		return value;
	}

	//------------------------------------------------------------------

	/**
	 * Adds some specified random data to the entropy pools of this PRNG.
	 * <p>
	 * {@code data} should come from an entropy source that supplies truly random data.
	 * </p>
	 *
	 * @param data  the random data that will be added to the entropy pools.
	 */

	public void addRandomBytes(byte[] data)
	{
		addRandomBytes(data, 0, data.length);
	}

	//------------------------------------------------------------------

	/**
	 * Adds a specified number of bytes of random data to a specified entropy pool of this PRNG.
	 *
	 * @param index   the index of the entropy pool to which the random data will be added.
	 * @param data    the random data that will be added to the specified entropy pool.
	 * @param offset  the offset of the start of the random data in {@code data}.
	 * @param length  the number of bytes of random data to add.
	 */

	public void addRandomBytes(int    index,
							   byte[] data,
							   int    offset,
							   int    length)
	{
		entropyPools[index].add(data, offset, length);
	}

	//------------------------------------------------------------------

	/**
	 * Creates an exclusive-OR combiner that uses this PRNG and has a specified block size.
	 *
	 * @param  blockSize  the number of bytes of random data that the combiner will extract from this PRNG
	 *                    with each request.
	 * @return an exclusive-OR combiner that uses this PRNG.
	 */

	public XorCombiner createCombiner(int blockSize)
	{
		return new XorCombiner(this, blockSize);
	}

	//------------------------------------------------------------------

	private void reseed()
	{
		lastReseedTime = System.currentTimeMillis();
		if (key != null)
			hash.update(key);
		++reseedIndex;
		int mask = 0;
		for (int i = 0; i < NUM_ENTROPY_POOLS; i++)
		{
			if ((reseedIndex & mask) != 0)
				break;
			hash.update(entropyPools[i].remove());
			mask <<= 1;
			++mask;
		}
		setKey(hash.digest());
	}

	//------------------------------------------------------------------

	private void setKey(byte[] key)
	{
		this.key = key;
		setCipherKey(key);
		incrementCounter();
	}

	//------------------------------------------------------------------

	private void generateBlock(byte[] buffer,
							   int    offset,
							   int    length)
	{
		int endOffset = offset + length;
		while (offset < endOffset)
		{
			int blockLength = Math.min(endOffset - offset, blockBuffer.length);
			if (blockLength < blockBuffer.length)
			{
				encryptCounter(blockBuffer, 0);
				System.arraycopy(blockBuffer, 0, buffer, offset, blockLength);
			}
			else
				encryptCounter(buffer, offset);
			incrementCounter();
			offset += blockLength;
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	private	int				keySize;
	private	ShaD256			hash;
	private	byte[]			key;
	private	byte[]			blockBuffer;
	private	EntropyPool[]	entropyPools;
	private	int				entropyPoolIndex;
	private	int				reseedIndex;
	private	long			lastReseedTime;

}

//----------------------------------------------------------------------
