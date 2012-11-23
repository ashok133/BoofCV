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

import static boofcv.misc.AutoTypeImage.*;


/**
 * Generates functions inside of {@link boofcv.alg.misc.ImageMiscOps}.
 *
 * @author Peter Abeles
 */
public class GeneratePixelMath extends CodeGeneratorBase {

	String className = "PixelMath";

	private AutoTypeImage input;

	public void generate() throws FileNotFoundException {
		printPreamble();
		printAllSigned();
		printAll();
		out.println("}");
	}

	private void printPreamble() throws FileNotFoundException {
		setOutputFile(className);
		out.print("import boofcv.struct.image.*;\n" +
				"\n" +
				"import boofcv.alg.InputSanityCheck;\n" +
				"\n" +
				"/**\n" +
				" * Functions which perform basic arithmetic (e.g. addition, subtraction, multiplication, or " +
				"division) on a pixel by pixel basis.\n" +
				" *\n" +
				" * <p>DO NOT MODIFY: Generated by {@link "+getClass().getName()+"}.</p>\n"+
				" *\n"+
				" * @author Peter Abeles\n" +
				" */\n" +
				"public class "+className+" {\n\n");
	}

	public void printAll() {
		AutoTypeImage types[] = AutoTypeImage.getSpecificTypes();

		for( AutoTypeImage t : types ) {
			input = t;
			printMultiply();
			printMultiplyBounded();
			printDivide();
			printDivideBounded();
			printPlus();
			printPlusBounded();
			printBoundImage();
			printDiffAbs();
			printAverageBand();
		}

		AutoTypeImage outputs[] = new AutoTypeImage[]{U16,S16,S32,S32,S32,S64,F32,F64};

		for( int i = 0; i < types.length; i++ ) {
			printAddTwoImages(types[i],outputs[i]);
			printSubtractTwoImages(types[i],outputs[i]);
		}
	}

	public void printAllSigned() {
		AutoTypeImage types[] = AutoTypeImage.getSigned();

		for( AutoTypeImage t : types ) {
			input = t;
			printAbs();
		}
	}

	public void printAbs()
	{
		out.print("\t/**\n" +
				"\t * Sets each pixel in the output image to be the absolute value of the input image.\n" +
				"\t * Both the input and output image can be the same instance.\n" +
				"\t * \n" +
				"\t * @param input The input image. Not modified.\n" +
				"\t * @param output Where the absolute value image is written to. Modified.\n" +
				"\t */\n" +
				"\tpublic static void abs( "+ input.getImageName()+" input , "+ input.getImageName()+" output ) {\n" +
				"\n" +
				"\t\tInputSanityCheck.checkSameShape(input,output);\n" +
				"\t\t\n" +
				"\t\tfor( int y = 0; y < input.height; y++ ) {\n" +
				"\t\t\tint indexSrc = input.startIndex + y* input.stride;\n" +
				"\t\t\tint indexDst = output.startIndex + y* output.stride;\n" +
				"\t\t\tint end = indexSrc + input.width;\n" +
				"\n" +
				"\t\t\tfor( ; indexSrc < end; indexSrc++ , indexDst++) {\n" +
				"\t\t\t\toutput.data[indexDst] = "+input.getTypeCastFromSum()+"Math.abs(input.data[indexSrc]);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printDivide() {
		String scaleType = input.isInteger() ? "double" : input.getSumType();
		String typeCast = scaleType.compareTo(input.getSumType()) != 0 ? "("+input.getDataType()+")" : "";

		out.print("\t/**\n" +
				"\t * Divide each element by a scalar value. Both input and output images can be the same instance.\n" +
				"\t *\n" +
				"\t * @param input The input image. Not modified.\n" +
				"\t * @param denominator What each element is divided by.\n" +
				"\t * @param output The output image. Modified.\n" +
				"\t */\n" +
				"\tpublic static void divide( "+input.getImageName()+" input , "+scaleType+" denominator , "+input.getImageName()+" output ) {\n" +
				"\n" +
				"\t\tInputSanityCheck.checkSameShape(input,output);\n" +
				"\n" +
				"\t\tfor( int y = 0; y < input.height; y++ ) {\n" +
				"\t\t\tint indexSrc = input.startIndex + y* input.stride;\n" +
				"\t\t\tint indexDst = output.startIndex + y* output.stride;\n" +
				"\t\t\tint end = indexSrc + input.width;\n" +
				"\n" +
				"\t\t\tfor( ; indexSrc < end; indexSrc++, indexDst++ ) {\n" +
				"\t\t\t\toutput.data[indexDst] = "+typeCast+"((input.data[indexSrc] "+input.getBitWise()+") / denominator);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printDivideBounded() {
		String scaleType = input.isInteger() ? "double" : input.getSumType();
		String sumType = input.getSumType();
		String typeCast = scaleType.compareTo(input.getSumType()) != 0 ? "("+input.getSumType()+")" : "";

		out.print("\t/**\n" +
				"\t * Divide each element by a scalar value and bounds the result. Both input and output images can be the same instance.\n" +
				"\t *\n" +
				"\t * @param input The input image. Not modified.\n" +
				"\t * @param denominator What each element is divided by.\n" +
				"\t * @param lower Lower bound on output\n" +
				"\t * @param upper Upper bound on output\n" +
				"\t * @param output The output image. Modified.\n" +
				"\t */\n" +
				"\tpublic static void divide( "+input.getImageName()+" input , "+scaleType+" denominator , "+
				sumType+" lower , "+sumType+" upper , "+input.getImageName()+" output ) {\n" +
				"\n" +
				"\t\tInputSanityCheck.checkSameShape(input,output);\n" +
				"\n" +
				"\t\tfor( int y = 0; y < input.height; y++ ) {\n" +
				"\t\t\tint indexSrc = input.startIndex + y* input.stride;\n" +
				"\t\t\tint indexDst = output.startIndex + y* output.stride;\n" +
				"\t\t\tint end = indexSrc + input.width;\n" +
				"\n" +
				"\t\t\tfor( ; indexSrc < end; indexSrc++, indexDst++ ) {\n" +
				"\t\t\t\t"+sumType+" val = "+typeCast+"((input.data[indexSrc] "+input.getBitWise()+") / denominator);\n" +
				"\t\t\t\tif( val < lower ) val = lower;\n" +
				"\t\t\t\tif( val > upper ) val = upper;\n" +
				"\t\t\t\toutput.data[indexDst] = "+input.getTypeCastFromSum()+"val;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}


	public void printMultiply() {
		String scaleType = input.isInteger() ? "double" : input.getSumType();
		String typeCast = scaleType.compareTo(input.getSumType()) != 0 ? "("+input.getDataType()+")" : "";

		out.print("\t/**\n" +
				"\t * Multiply each element by a scalar value. Both input and output images can\n" +
				"\t * be the same instance.\n" +
				"\t *\n" +
				"\t * @param input The input image. Not modified.\n" +
				"\t * @param value What each element is multiplied by.\n" +
				"\t * @param output The output image. Modified.\n" +
				"\t */\n" +
				"\tpublic static void multiply( "+input.getImageName()+" input , "+scaleType+" value , "+input.getImageName()+" output ) {\n" +
				"\n" +
				"\t\tInputSanityCheck.checkSameShape(input,output);\n" +
				"\n" +
				"\t\tfor( int y = 0; y < input.height; y++ ) {\n" +
				"\t\t\tint indexSrc = input.startIndex + y* input.stride;\n" +
				"\t\t\tint indexDst = output.startIndex + y* output.stride;\n" +
				"\t\t\tint end = indexSrc + input.width;\n" +
				"\n" +
				"\t\t\tfor( ; indexSrc < end; indexSrc++, indexDst++ ) {\n" +
				"\t\t\t\toutput.data[indexDst] = "+typeCast+"((input.data[indexSrc] "+input.getBitWise()+") * value);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printMultiplyBounded() {
		String scaleType = input.isInteger() ? "double" : input.getSumType();
		String sumType = input.getSumType();
		String typeCast = scaleType.compareTo(input.getSumType()) != 0 ? "("+input.getSumType()+")" : "";

		out.print("\t/**\n" +
				"\t * Multiply each element by a scalar value and bounds the result. Both input and output images can\n" +
				"\t * be the same instance.\n" +
				"\t *\n" +
				"\t * @param input The input image. Not modified.\n" +
				"\t * @param value What each element is multiplied by.\n" +
				"\t * @param lower Lower bound on output\n" +
				"\t * @param upper Upper bound on output\n" +
				"\t * @param output The output image. Modified.\n" +
				"\t */\n" +
				"\tpublic static void multiply( "+input.getImageName()+" input , "+scaleType+" value , " +
				sumType+" lower , "+sumType+" upper , "+input.getImageName()+" output ) {\n" +
				"\n" +
				"\t\tInputSanityCheck.checkSameShape(input,output);\n" +
				"\n" +
				"\t\tfor( int y = 0; y < input.height; y++ ) {\n" +
				"\t\t\tint indexSrc = input.startIndex + y* input.stride;\n" +
				"\t\t\tint indexDst = output.startIndex + y* output.stride;\n" +
				"\t\t\tint end = indexSrc + input.width;\n" +
				"\n" +
				"\t\t\tfor( ; indexSrc < end; indexSrc++, indexDst++ ) {\n" +
				"\t\t\t\t"+sumType+" val = "+typeCast+"((input.data[indexSrc] "+input.getBitWise()+") * value);\n" +
				"\t\t\t\tif( val < lower ) val = lower;\n" +
				"\t\t\t\tif( val > upper ) val = upper;\n" +
				"\t\t\t\toutput.data[indexDst] = "+input.getTypeCastFromSum()+"val;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printPlus() {
		out.print("\t/**\n" +
				"\t * Add a scalar value to each element. Both input and output images can be the same instance.\n" +
				"\t *\n" +
				"\t * @param input The input image. Not modified.\n" +
				"\t * @param value What is added to each element.\n" +
				"\t * @param output The output image. Modified.\n" +
				"\t */\n" +
				"\tpublic static void plus( "+input.getImageName()+" input , "+input.getSumType()+" value , "+input.getImageName()+" output ) {\n" +
				"\n" +
				"\t\tInputSanityCheck.checkSameShape(input,output);\n" +
				"\n" +
				"\t\tfor( int y = 0; y < input.height; y++ ) {\n" +
				"\t\t\tint indexSrc = input.startIndex + y* input.stride;\n" +
				"\t\t\tint indexDst = output.startIndex + y* output.stride;\n" +
				"\t\t\tint end = indexSrc + input.width;\n" +
				"\n" +
				"\t\t\tfor( ; indexSrc < end; indexSrc++, indexDst++ ) {\n" +
				"\t\t\t\toutput.data[indexDst] = "+input.getTypeCastFromSum()+"((input.data[indexSrc] "+input.getBitWise()+") + value);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printPlusBounded() {
		String sumType = input.getSumType();

		out.print("\t/**\n" +
				"\t * Add a scalar value to each element and bounds the result. Both input and output images can be the same instance.\n" +
				"\t *\n" +
				"\t * @param input The input image. Not modified.\n" +
				"\t * @param value What is added to each element.\n" +
				"\t * @param lower Lower bound on output\n" +
				"\t * @param upper Upper bound on output\n" +
				"\t * @param output The output image. Modified.\n" +
				"\t */\n" +
				"\tpublic static void plus( "+input.getImageName()+" input , "+sumType+" value , "+
				sumType+" lower , "+sumType+" upper , "+input.getImageName()+" output ) {\n" +
				"\n" +
				"\t\tInputSanityCheck.checkSameShape(input,output);\n" +
				"\n" +
				"\t\tfor( int y = 0; y < input.height; y++ ) {\n" +
				"\t\t\tint indexSrc = input.startIndex + y* input.stride;\n" +
				"\t\t\tint indexDst = output.startIndex + y* output.stride;\n" +
				"\t\t\tint end = indexSrc + input.width;\n" +
				"\n" +
				"\t\t\tfor( ; indexSrc < end; indexSrc++, indexDst++ ) {\n" +
				"\t\t\t\t"+sumType+" val = (input.data[indexSrc] "+input.getBitWise()+") + value;\n" +
				"\t\t\t\tif( val < lower ) val = lower;\n" +
				"\t\t\t\tif( val > upper ) val = upper;\n" +
				"\t\t\t\toutput.data[indexDst] = "+input.getTypeCastFromSum()+"val;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printBoundImage() {

		String bitWise = input.getBitWise();
		String sumType = input.getSumType();

		out.print("\t/**\n" +
				"\t * Bounds image pixels to be between these two values\n" +
				"\t * \n" +
				"\t * @param img Image\n" +
				"\t * @param min minimum value.\n" +
				"\t * @param max maximum value.\n" +
				"\t */\n" +
				"\tpublic static void boundImage( "+input.getImageName()+" img , "+sumType+" min , "+sumType+" max ) {\n" +
				"\t\tfinal int h = img.getHeight();\n" +
				"\t\tfinal int w = img.getWidth();\n" +
				"\n" +
				"\t\t"+input.getDataType()+"[] data = img.data;\n" +
				"\n" +
				"\t\tfor (int y = 0; y < h; y++) {\n" +
				"\t\t\tint index = img.getStartIndex() + y * img.getStride();\n" +
				"\t\t\tint indexEnd = index+w;\n" +
				"\t\t\t// for(int x = 0; x < w; x++ ) {\n" +
				"\t\t\tfor (; index < indexEnd; index++) {\n" +
				"\t\t\t\t"+sumType+" value = data[index]"+bitWise+";\n" +
				"\t\t\t\tif( value < min )\n" +
				"\t\t\t\t\tdata[index] = "+input.getTypeCastFromSum()+"min;\n" +
				"\t\t\t\telse if( value > max )\n" +
				"\t\t\t\t\tdata[index] = "+input.getTypeCastFromSum()+"max;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printDiffAbs() {

		String bitWise = input.getBitWise();
		String typeCast = input.isInteger() ? "("+input.getDataType()+")" : "";

		out.print("\t/**\n" +
				"\t * <p>\n" +
				"\t * Computes the absolute value of the difference between each pixel in the two images.<br>\n" +
				"\t * d(x,y) = |img1(x,y) - img2(x,y)|\n" +
				"\t * </p>\n" +
				"\t * @param imgA Input image. Not modified.\n" +
				"\t * @param imgB Input image. Not modified.\n" +
				"\t * @param diff Absolute value of difference image. Modified.\n" +
				"\t */\n" +
				"\tpublic static void diffAbs( "+input.getImageName()+" imgA , "+input.getImageName()+" imgB , "+input.getImageName()+" diff ) {\n" +
				"\t\tInputSanityCheck.checkSameShape(imgA,imgB,diff);\n" +
				"\t\t\n" +
				"\t\tfinal int h = imgA.getHeight();\n" +
				"\t\tfinal int w = imgA.getWidth();\n" +
				"\n" +
				"\t\tfor (int y = 0; y < h; y++) {\n" +
				"\t\t\tint indexA = imgA.getStartIndex() + y * imgA.getStride();\n" +
				"\t\t\tint indexB = imgB.getStartIndex() + y * imgB.getStride();\n" +
				"\t\t\tint indexDiff = diff.getStartIndex() + y * diff.getStride();\n" +
				"\t\t\t\n" +
				"\t\t\tint indexEnd = indexA+w;\n" +
				"\t\t\t// for(int x = 0; x < w; x++ ) {\n" +
				"\t\t\tfor (; indexA < indexEnd; indexA++, indexB++, indexDiff++ ) {\n" +
				"\t\t\t\tdiff.data[indexDiff] = "+typeCast+"Math.abs((imgA.data[indexA] "+bitWise+") - (imgB.data[indexB] "+bitWise+"));\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printAddTwoImages( AutoTypeImage typeIn , AutoTypeImage typeOut  ) {

		String bitWise = typeIn.getBitWise();
		String typeCast = typeOut.isInteger() ? "("+typeOut.getDataType()+")" : "";

		out.print("\t/**\n" +
				"\t * <p>\n" +
				"\t * Performs pixel-wise addition<br>\n" +
				"\t * d(x,y) = imgA(x,y) + imgB(x,y)\n" +
				"\t * </p>\n" +
				"\t * @param imgA Input image. Not modified.\n" +
				"\t * @param imgB Input image. Not modified.\n" +
				"\t * @param output Output image. Modified.\n" +
				"\t */\n" +
				"\tpublic static void add( "+typeIn.getImageName()+" imgA , "+typeIn.getImageName()+" imgB , "+typeOut.getImageName()+" output ) {\n" +
				"\t\tInputSanityCheck.checkSameShape(imgA,imgB,output);\n" +
				"\t\t\n" +
				"\t\tfinal int h = imgA.getHeight();\n" +
				"\t\tfinal int w = imgA.getWidth();\n" +
				"\n" +
				"\t\tfor (int y = 0; y < h; y++) {\n" +
				"\t\t\tint indexA = imgA.getStartIndex() + y * imgA.getStride();\n" +
				"\t\t\tint indexB = imgB.getStartIndex() + y * imgB.getStride();\n" +
				"\t\t\tint indexOut = output.getStartIndex() + y * output.getStride();\n" +
				"\t\t\t\n" +
				"\t\t\tint indexEnd = indexA+w;\n" +
				"\t\t\t// for(int x = 0; x < w; x++ ) {\n" +
				"\t\t\tfor (; indexA < indexEnd; indexA++, indexB++, indexOut++ ) {\n" +
				"\t\t\t\toutput.data[indexOut] = "+typeCast+"((imgA.data[indexA] "+bitWise+") + (imgB.data[indexB] "+bitWise+"));\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printSubtractTwoImages( AutoTypeImage typeIn , AutoTypeImage typeOut ) {

		String bitWise = typeIn.getBitWise();
		String typeCast = typeOut.isInteger() ? "("+typeOut.getDataType()+")" : "";

		out.print("\t/**\n" +
				"\t * <p>\n" +
				"\t * Performs pixel-wise subtraction, but ensures the result is between two bounds.<br>\n" +
				"\t * d(x,y) = imgA(x,y) - imgB(x,y)\n" +
				"\t * </p>\n" +
				"\t * @param imgA Input image. Not modified.\n" +
				"\t * @param imgB Input image. Not modified.\n" +
				"\t * @param output Output image. Modified.\n" +
				"\t */\n" +
				"\tpublic static void subtract( "+typeIn.getImageName()+" imgA , "+typeIn.getImageName()+" imgB , "
				+typeOut.getImageName()+" output ) {\n" +
				"\t\tInputSanityCheck.checkSameShape(imgA,imgB,output);\n" +
				"\t\t\n" +
				"\t\tfinal int h = imgA.getHeight();\n" +
				"\t\tfinal int w = imgA.getWidth();\n" +
				"\n" +
				"\t\tfor (int y = 0; y < h; y++) {\n" +
				"\t\t\tint indexA = imgA.getStartIndex() + y * imgA.getStride();\n" +
				"\t\t\tint indexB = imgB.getStartIndex() + y * imgB.getStride();\n" +
				"\t\t\tint indexOut = output.getStartIndex() + y * output.getStride();\n" +
				"\t\t\t\n" +
				"\t\t\tint indexEnd = indexA+w;\n" +
				"\t\t\t// for(int x = 0; x < w; x++ ) {\n" +
				"\t\t\tfor (; indexA < indexEnd; indexA++, indexB++, indexOut++ ) {\n" +
				"\t\t\t\toutput.data[indexOut] = "+typeCast+"((imgA.data[indexA] "+bitWise+") - (imgB.data[indexB] "+bitWise+"));\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public void printAverageBand() {
		
		String imageName = input.getImageName();
		String sumType = input.getSumType();
		String typecast = input.getTypeCastFromSum();
		String bitwise = input.getBitWise();
		
		out.print("\t/**\n" +
				"\t * Computes the average for each pixel across all bands in the {@link MultiSpectral} image.\n" +
				"\t * \n" +
				"\t * @param input MultiSpectral image\n" +
				"\t * @param output Gray scale image containing average pixel values\n" +
				"\t */\n" +
				"\tpublic static void averageBand( MultiSpectral<"+imageName+"> input , "+imageName+" output ) {\n" +
				"\t\tfinal int h = input.getHeight();\n" +
				"\t\tfinal int w = input.getWidth();\n" +
				"\n" +
				"\t\t"+imageName+"[] bands = input.bands;\n" +
				"\t\t\n" +
				"\t\tfor (int y = 0; y < h; y++) {\n" +
				"\t\t\tint indexInput = input.getStartIndex() + y * input.getStride();\n" +
				"\t\t\tint indexOutput = output.getStartIndex() + y * output.getStride();\n" +
				"\n" +
				"\t\t\tint indexEnd = indexInput+w;\n" +
				"\t\t\t// for(int x = 0; x < w; x++ ) {\n" +
				"\t\t\tfor (; indexInput < indexEnd; indexInput++, indexOutput++ ) {\n" +
				"\t\t\t\t"+sumType+" total = 0;\n" +
				"\t\t\t\tfor( int i = 0; i < bands.length; i++ ) {\n" +
				"\t\t\t\t\ttotal += bands[i].data[ indexInput ]"+bitwise+";\n" +
				"\t\t\t\t}\n" +
				"\t\t\t\toutput.data[indexOutput] = "+typecast+"(total / bands.length);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public static void main( String args[] ) throws FileNotFoundException {
		GeneratePixelMath gen = new GeneratePixelMath();
		gen.generate();
	}
}
