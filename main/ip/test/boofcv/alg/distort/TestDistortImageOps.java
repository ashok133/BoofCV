/*
 * Copyright (c) 2011, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://www.boofcv.org).
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

package boofcv.alg.distort;

import boofcv.alg.interpolate.InterpolatePixel;
import boofcv.alg.interpolate.TypeInterpolate;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.factory.interpolate.FactoryInterpolation;
import boofcv.struct.image.ImageFloat32;
import boofcv.testing.BoofTesting;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;


/**
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public class TestDistortImageOps {

	Random rand = new Random(234);
	int width = 20;
	int height = 30;

	/**
	 * Checks to see if the two ways of specifying interpolation work
	 */
	@Test
	public void scale_InterpTypeStyle() {
		ImageFloat32 input = new ImageFloat32(width,height);
		ImageFloat32 output = new ImageFloat32(width,height);
		ImageFloat32 output2 = new ImageFloat32(width,height);

		GeneralizedImageOps.randomize(input,rand,0,100);

		InterpolatePixel<ImageFloat32> interp = FactoryInterpolation.bilinearPixel(input);

		// check the two scale function
		DistortImageOps.scale(input,output,interp);
		DistortImageOps.scale(input,output2, TypeInterpolate.BILINEAR);

		// they should be identical
		BoofTesting.assertEquals(output,output2);

		interp.setImage(input);

		float scaleX = (float)input.width/(float)output.width;
		float scaleY = (float)input.height/(float)output.height;

		if( input.getTypeInfo().isInteger() ) {
			for( int i = 0; i < output.height; i++ ) {
				for( int j = 0; j < output.width; j++ ) {
					float val = interp.get(j*scaleX,i*scaleY);
					assertEquals((int)val,output.get(j,i),1e-4);
				}
			}
		} else {
			for( int i = 0; i < output.height; i++ ) {
				for( int j = 0; j < output.width; j++ ) {
					float val = interp.get(j*scaleX,i*scaleY);
					assertEquals(val,output.get(j,i),1e-4);
				}
			}
		}
	}

	@Test
	public void scaleSanityCheck() {
		fail("write");
	}

	@Test
	public void rotate_InterpTypeStyle() {
		ImageFloat32 input = new ImageFloat32(width,height);
		ImageFloat32 output = new ImageFloat32(width,height);
		ImageFloat32 output2 = new ImageFloat32(width,height);

		GeneralizedImageOps.randomize(input,rand,0,100);

		InterpolatePixel<ImageFloat32> interp = FactoryInterpolation.bilinearPixel(input);


		DistortImageOps.rotate(input,output,interp,(float)Math.PI/2f);
		DistortImageOps.rotate(input,output2,TypeInterpolate.BILINEAR,(float)Math.PI/2f);

		// they should be identical
		BoofTesting.assertEquals(output,output2);
	}

	/**
	 * Very simple test for rotation accuracy.
	 */
	@Test
	public void rotate_SanityCheck() {
		ImageFloat32 input = new ImageFloat32(width,height);
		ImageFloat32 output = new ImageFloat32(height,width);

		GeneralizedImageOps.randomize(input,rand,0,100);

		DistortImageOps.rotate(input, output, TypeInterpolate.BILINEAR, (float) Math.PI / 2f);

		double error = 0;
		// the outside pixels are ignored because numerical round off can cause those to be skipped
		for( int y = 1; y < input.height-1; y++ ) {
			for( int x = 1; x < input.width-1; x++ ) {
				int xx = output.width-y;
				int yy = x;

				double e = input.get(x,y)-output.get(xx,yy);
				error += Math.abs(e);
			}
		}
		assertTrue(error / (width * height) < 0.1);
	}
}