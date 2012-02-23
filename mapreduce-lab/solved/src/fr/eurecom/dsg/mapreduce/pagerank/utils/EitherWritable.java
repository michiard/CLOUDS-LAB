package fr.eurecom.dsg.mapreduce.pagerank.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Writable;

/**
 *  Container that contains an object either of TLEFT or TRIGHT type 
 *
 * @param <TLEFT>
 * @param <TRIGHT>
 */
@SuppressWarnings("unchecked")
public abstract class EitherWritable<TLEFT extends Writable, TRIGHT extends Writable>
		implements Writable {

	public final static boolean LEFT = false;
	public final static boolean RIGHT = true;

	private boolean whatis;

	public boolean isLeft() {
		return this.whatis == LEFT;
	}

	public boolean isRight() {
		return this.whatis == RIGHT;
	}

	private TLEFT leftValue;
	private TRIGHT rightValue;

	public TLEFT left() {
		return this.leftValue;
	}

	public TRIGHT right() {
		return this.rightValue;
	}

	private Class<TLEFT> leftClass;
	private Class<TRIGHT> rightClass;

	public EitherWritable() {
		this.setClasses();
	}

	public abstract void setClasses();

	public void setClasses(Class<TLEFT> leftClass, Class<TRIGHT> rightClass) {
		this.leftClass = leftClass;
		this.rightClass = rightClass;
	}

	public Class<TLEFT> getLeftClass() {
		return leftClass;
	}

	public Class<TRIGHT> getRightClass() {
		return rightClass;
	}

	public void setLeft(TLEFT leftValue) {
		this.whatis = LEFT;
		this.leftValue = leftValue;
		this.rightValue = null;
	}

	public void setRight(TRIGHT rightValue) {
		this.whatis = RIGHT;
		this.leftValue = null;
		this.rightValue = rightValue;
	}

	//
	// @SuppressWarnings("unchecked")
	// public static <TLEFT extends Writable, TRIGHT extends Writable>
	// EitherWritable<TLEFT, TRIGHT> makeLeft(
	// TLEFT leftValue, Class<TRIGHT> rightClass) {
	// EitherWritable<TLEFT, TRIGHT> either;
	// either.setLeft(leftValue);
	// return either;
	// }
	//
	// @SuppressWarnings("unchecked")
	// public static <TLEFT extends Writable, TRIGHT extends Writable>
	// EitherWritable<TLEFT, TRIGHT> makeRight(
	// Class<TLEFT> leftClass, TRIGHT rightValue) {
	// EitherWritable<TLEFT, TRIGHT> either = new EitherWritable<TLEFT, TRIGHT>(
	// leftClass, (Class<TRIGHT>) rightValue.getClass());
	// either.setRight(rightValue);
	// return either;
	// }

	@Override
	public void readFields(DataInput in) throws IOException {
		this.whatis = in.readBoolean();

		if (this.isLeft()) {
			this.leftValue = this.readLeftValue(in);
		} else {
			this.rightValue = this.readRightValue(in);
		}

	}

	protected TLEFT readLeftValue(DataInput in) throws IOException {
		try {
			TLEFT value = this.leftClass.newInstance();
			value.readFields(in);
			return value;
		} catch (InstantiationException e) {
			throw new IOException(e);
		} catch (IllegalAccessException e) {
			throw new IOException(e);
		}

	}

	protected TRIGHT readRightValue(DataInput in) throws IOException {
		try {
			TRIGHT value = this.rightClass.newInstance();
			value.readFields(in);
			return value;
		} catch (InstantiationException e) {
			throw new IOException(e);
		} catch (IllegalAccessException e) {
			throw new IOException(e);
		}

	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeBoolean(this.whatis);
		if (this.isLeft())
			this.left().write(out);
		else
			this.right().write(out);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (obj.getClass() != this.getClass())
			return false;

		EitherWritable<TLEFT, TRIGHT> either = (EitherWritable<TLEFT, TRIGHT>) obj;

		return this.whatis == either.whatis
				&& (this.isLeft() ? this.left().equals(either.left()) : this
						.right().equals(either.right()));
	}

	@Override
	public int hashCode() {

		return this.isLeft() ? this.left().hashCode()
				: this.right().hashCode() * 163;
	}

	public static class EitherNodeFloatWritable extends
			EitherWritable<NodeWritable, FloatWritable> {

		@Override
		public void setClasses() {
			this.setClasses(NodeWritable.class, FloatWritable.class);
		}

		public void setNode(NodeWritable node) {
			this.setLeft(node);
		}
		
		public NodeWritable node() {
			return this.left();
		}
		
		public boolean isNode() {
			return this.isLeft();
		}
		
		public void setFloat(FloatWritable f) {
			this.setRight(f);
		}
		
		public FloatWritable Float() {
			return this.right();
		}
		
		public boolean isFloat() {
			return this.isRight();
		}
	}

	public static class EitherNodeBoolWritable extends
			EitherWritable<NodeWritable, BooleanWritable> {

		@Override
		public void setClasses() {
			this.setClasses(NodeWritable.class, BooleanWritable.class);
		}
		
		public void setNode(NodeWritable node) {
			this.setLeft(node);
		}
		
		public NodeWritable node() {
			return this.left();
		}
		
		public boolean isNode() {
			return this.isLeft();
		}
		
		public void setBoolean(BooleanWritable bool) {
			this.setRight(bool);
		}
		
		public BooleanWritable Boolean() {
			return this.right();
		}
		
		public boolean isBoolean() {
			return this.isRight();
		}
	}

//	public final static void main(String[] args) throws IOException {
//		EitherTextLongWritable either = new EitherTextLongWritable();
//		either.setLeft(new Text("Text"));
//		DataOutputBuffer out = new DataOutputBuffer();
//		either.write(out);
//		DataInputBuffer in = new DataInputBuffer();
//		in.reset(out.getData(), out.getData().length);
//		EitherTextLongWritable either2 = new EitherTextLongWritable();
//
//		either2.readFields(in);
//
//		System.out.println(either2.isLeft() ? either2.left() : either2.right());
//
//		System.out.println(either.equals(either2));
//	}
}
