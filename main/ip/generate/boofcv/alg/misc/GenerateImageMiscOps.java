/*
 * Copyright (c) 2011-2012, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.alg.misc;

import boofcv.misc.AutoTypeImage;
import boofcv.misc.CodeGeneratorBase;

import java.io.FileNotFoundException;


/**
 * Generates functions inside of {@link boofcv.alg.misc.ImageMiscOps}.
 *
 * @author Peter Abeles
 */
public class GenerateImageMiscOps extends CodeGeneratorBase {

	String className = "ImageMiscOps";

	private AutoTypeImage imageType;
	private String imageName;
	private String dataType;
	private String bitWise;

	public void generate() throws FileNotFoundException {
		printPreamble();
		printAllGeneric();
		printAllSpecific();
		out.println("}");
	}

	private void printPreamble() throws FileNotFoundException {
		setOutputFile(className);
		out.print("import boofcv.struct.image.*;\n" +
				"\n" +
				"import java.util.Random;\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Basic image operations which have no place better to go.\n" +
				" *\n" +
				" * <p>DO NOT MODIFY: Generated by {@link "+getClass().getName()+"}.</p>\n"+
				" *\n"+
				" * @author Peter Abeles\n" +
				" */\n" +
				"public class "+className+" {\n\n");
	}

	public void printAllGeneric() {
		AutoTypeImage types[] = AutoTypeImage.getGenericTypes();

		for( AutoTypeImage t : types ) {
			imageType = t;
			imageName = t.getImageName();
			dataType = t.getDataType();
			printFill();
			printFillRectangle();
			printFillUniform();
			printFillGaussian();
			printFlipVertical();
		}
	}

	public void printAllSpecific() {
		AutoTypeImage types[] = AutoTypeImage.getSpecificTypes();

		for( AutoTypeImage t : types ) {
			imageType = t;
			imageName = t.getImageName();
			dataType = t.getDataType();
			bitWise = t.getBitWise();
			printAddUniform();
			printAddGaussian();
		}
	}

	public void printFill()
	{
		String typeCast = imageType.getTypeCastFromSum();
		out.print("\t/**\n" +
				"\t * Fills the whole image with the specified value\n" +
				"\t *\n" +
				"\t * @param input An image.\n" +
				"\t * @param value The value that the image is being filled with.\n" +
				"\t */\n" +
				"\tpublic static void fill("+imageName+" input, "+imageType.getSumType()+" value) {\n" +
				"\n" +
				"\t\tfor (int y = 0; y < input.height; y++) {\n" +
				"\t\t\tint index = input.getStartIndex() + y * input.getStride();\n" +
				"\t\t\tfor (int x = 0; x < input.width; x++) {\n" +
				"\t\t\t\tinput.data[index++] = "+typeCast+"value;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printFillRectangle()
	{
		out.print("\t/**\n" +
				"\t * Draws a filled rectangle that is aligned along the image axis inside the image.\n" +
				"\t *\n" +
				"\t * @param img Image the rectangle is drawn in.  Modified\n" +
				"\t * @param value Value of the rectangle\n" +
				"\t * @param x0 Top left x-coordinate\n" +
				"\t * @param y0 Top left y-coordinate\n" +
				"\t * @param width Rectangle width\n" +
				"\t * @param height Rectangle height\n" +
				"\t */\n" +
				"\tpublic static void fillRectangle("+imageName+" img, "+imageType.getSumType()+" value, int x0, int y0, int width, int height) {\n" +
				"\t\tint x1 = x0 + width;\n" +
				"\t\tint y1 = y0 + height;\n" +
				"\n" +
				"\t\tfor (int y = y0; y < y1; y++) {\n" +
				"\t\t\tfor (int x = x0; x < x1; x++) {\n" +
				"\t\t\t\tif( img.isInBounds(x,y ))\n" +
				"\t\t\t\t\timg.set(x, y, value);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printFillUniform() {

		String sumType = imageType.getSumType();
		String typeCast = imageType.getTypeCastFromSum();

		out.print("\t/**\n" +
				"\t * Sets each value in the image to a value drawn from an uniform distribution that has a range of min <= X < max.\n" +
				"\t *\n" +
				"\t * @param img Image which is to be filled.  Modified,\n" +
				"\t * @param rand Random number generator\n" +
				"\t * @param min Minimum value of the distribution\n" +
				"\t * @param max Maximum value of the distribution\n" +
				"\t */\n" +
				"\tpublic static void fillUniform("+imageName+" img, Random rand , "+sumType+" min , "+sumType+" max) {\n" +
				"\t\t"+sumType+" range = max-min;\n" +
				"\n" +
				"\t\t"+dataType+"[] data = img.data;\n" +
				"\n" +
				"\t\tfor (int y = 0; y < img.height; y++) {\n" +
				"\t\t\tint index = img.getStartIndex() + y * img.getStride();\n" +
				"\t\t\tfor (int x = 0; x < img.width; x++) {\n");
		if( imageType.isInteger() && imageType.getNumBits() < 64) {
			out.print("\t\t\t\tdata[index++] = "+typeCast+"(rand.nextInt(range)+min);\n");
		} else if( imageType.isInteger() ) {
			out.print("\t\t\t\tdata[index++] = rand.nextInt((int)range)+min;\n");
		} else {
			String randType = imageType.getRandType();
			out.print("\t\t\t\tdata[index++] = rand.next"+randType+"()*range+min;\n");
		}
		out.print("\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printFillGaussian() {

		String sumType = imageType.getSumType();
		String castToSum = sumType.compareTo("double") == 0 ? "" : "("+sumType+")";
		String typeCast = imageType.getTypeCastFromSum();

		out.print("\t/**\n" +
				"\t * Sets each value in the image to a value drawn from a Gaussian distribution.  A user\n" +
				"\t * specified lower and upper bound is provided to ensure that the values are within a legal\n" +
				"\t * range.  A drawn value outside the allowed range will be set to the closest bound.\n" +
				"\t * \n" +
				"\t * @param input Input image.  Modified.\n" +
				"\t * @param rand Random number generator\n" +
				"\t * @param mean Distribution's mean.\n" +
				"\t * @param sigma Distribution's standard deviation.\n" +
				"\t * @param lowerBound Lower bound of value clip\n" +
				"\t * @param upperBound Upper bound of value clip\n" +
				"\t */\n" +
				"\tpublic static void fillGaussian("+imageName+" input, Random rand , double mean , double sigma , "
				+sumType+" lowerBound , "+sumType+" upperBound ) {\n" +
				"\t\t"+dataType+"[] data = input.data;\n" +
				"\n" +
				"\t\tfor (int y = 0; y < input.height; y++) {\n" +
				"\t\t\tint index = input.getStartIndex() + y * input.getStride();\n" +
				"\t\t\tfor (int x = 0; x < input.width; x++) {\n" +
				"\t\t\t\t"+sumType+" value = "+castToSum+"(rand.nextGaussian()*sigma+mean);\n" +
				"\t\t\t\tif( value < lowerBound ) value = lowerBound;\n" +
				"\t\t\t\tif( value > upperBound ) value = upperBound;\n" +
				"\t\t\t\tdata[index++] = "+typeCast+"value;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printAddUniform() {

		String sumType = imageType.getSumType();
		int min = imageType.getMin().intValue();
		int max = imageType.getMax().intValue();
		String typeCast = imageType.getTypeCastFromSum();

		out.print("\t/**\n" +
				"\t * Adds uniform i.i.d noise to each pixel in the image.  Noise range is min <= X < max.\n" +
				"\t */\n" +
				"\tpublic static void addUniform("+imageName+" input, Random rand , "+sumType+" min , "+sumType+" max) {\n" +
				"\t\t"+sumType+" range = max-min;\n" +
				"\n" +
				"\t\t"+dataType+"[] data = input.data;\n" +
				"\n" +
				"\t\tfor (int y = 0; y < input.height; y++) {\n" +
				"\t\t\tint index = input.getStartIndex() + y * input.getStride();\n" +
				"\t\t\tfor (int x = 0; x < input.width; x++) {\n");
		if( imageType.isInteger() && imageType.getNumBits() != 64) {
			out.print("\t\t\t\t"+sumType+" value = (data[index] "+bitWise+") + rand.nextInt(range)+min;\n");
			if( imageType.getNumBits() < 32 ) {
				out.print("\t\t\t\tif( value < "+min+" ) value = "+min+";\n" +
						"\t\t\t\tif( value > "+max+" ) value = "+max+";\n" +
						"\n");
			}
		} else if( imageType.isInteger() ) {
			out.print("\t\t\t\t"+sumType+" value = data[index] + rand.nextInt((int)range)+min;\n");
		} else {
			String randType = imageType.getRandType();
			out.print("\t\t\t\t"+sumType+" value = data[index] + rand.next"+randType+"()*range+min;\n");
		}
		out.print("\t\t\t\tdata[index++] = "+typeCast+" value;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printAddGaussian() {
		String sumType = imageType.getSumType();
		String typeCast = imageType.getTypeCastFromSum();
		String sumCast = sumType.equals("double") ? "" : "("+sumType+")";

		out.print("\t/**\n" +
				"\t * Adds Gaussian/normal i.i.d noise to each pixel in the image.  If a value exceeds the specified\n"+
				"\t * it will be set to the closest bound.\n" +
				"\t * @param input Input image.  Modified.\n" +
				"\t * @param rand Random number generator.\n" +
				"\t * @param sigma Distributions standard deviation.\n" +
				"\t * @param lowerBound Allowed lower bound\n" +
				"\t * @param upperBound Allowed upper bound\n" +
				"\t */\n" +
				"\tpublic static void addGaussian("+imageName+" input, Random rand , double sigma , "
				+sumType+" lowerBound , "+sumType+" upperBound ) {\n" +
				"\n" +
				"\t\tfor (int y = 0; y < input.height; y++) {\n" +
				"\t\t\tint index = input.getStartIndex() + y * input.getStride();\n" +
				"\t\t\tfor (int x = 0; x < input.width; x++) {\n" +
				"\t\t\t\t"+sumType+" value = (input.data[index] "+bitWise+") + "+sumCast+"(rand.nextGaussian()*sigma);\n" +
				"\t\t\t\tif( value < lowerBound ) value = lowerBound;\n" +
				"\t\t\t\tif( value > upperBound ) value = upperBound;\n" +
				"\t\t\t\tinput.data[index++] = "+typeCast+" value;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printFlipVertical() {
		String sumType = imageType.getSumType();

		out.print("\t/**\n" +
				"\t * Flips the image from top to bottom\n" +
				"\t */\n" +
				"\tpublic static void flipVertical( "+imageName+" input ) {\n" +
				"\t\tint h2 = input.height/2;\n" +
				"\n" +
				"\t\tfor( int y = 0; y < h2; y++ ) {\n" +
				"\t\t\tint index1 = input.getStartIndex() + y * input.getStride();\n" +
				"\t\t\tint index2 = input.getStartIndex() + (input.height - y - 1) * input.getStride();\n" +
				"\n" +
				"\t\t\tint end = index1 + input.width;\n" +
				"\n" +
				"\t\t\twhile( index1 < end ) {\n" +
				"\t\t\t\t"+sumType+" tmp = input.data[index1];\n" +
				"\t\t\t\tinput.data[index1++] = input.data[index2];\n" +
				"\t\t\t\tinput.data[index2++] = ("+dataType+")tmp;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}


	public static void main( String args[] ) throws FileNotFoundException {
		GenerateImageMiscOps gen = new GenerateImageMiscOps();
		gen.generate();
	}
}
