package com.graphics.engine.graphics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashSet;

/**
 * 
 * <br>类描述: 硬编码的（框架预定义的）shader源代码
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-1-24]
 * @formatter:off
 */
class ShaderStrings {
	
	final static String STRING_HARD_CODE_FILE_NAME = "/sdcard/shader_source_string.txt";
	static boolean sFirstOpenStringHardCodeFile = true;
	static HashSet<String> sSourceFileNameSet = new HashSet<String>();
	
	/**
	 * <br>功能简述: 将已加载的shader源代码转换成硬编码字符串
	 * <br>功能详细描述: 在shader开发阶段，使用读取文件的方式加载代码，开发完成之后可以使用本方法转化为硬编码方式以免去对IO的依赖
	 * <br>注意:
	 * @param source
	 * @param sourceFileName
	 */
	static void convertSourceCodeToHardCodingString(String source, String sourceFileName) {
		try {
			if (!sSourceFileNameSet.add(sourceFileName)) {
				return;
			}
			
			FileWriter fw = new FileWriter(STRING_HARD_CODE_FILE_NAME, !sFirstOpenStringHardCodeFile);
			sFirstOpenStringHardCodeFile = false;
			BufferedWriter bfw = new BufferedWriter(fw);
			
			bfw.write("//================ " + sourceFileName + " ================\n");
			bfw.write("final static String " + sourceFileName.toUpperCase().replace('.', '_') + " = \n");
			
			String[] lines = source.split("\n");
			for (int i = 0; i < lines.length; ++i) {
				String str = "\t\"" + lines[i] + "\" + \"\\n\" + \n";
				bfw.write(str);
			}
			
			bfw.write("\t\"\";\n\n");
			bfw.close();
			fw.close();
		}
		catch (Exception e) {
		}
	}
	
	//================ 下面是已经转换好的字符串 ================
	
	//================ texture.vert ================
	final static String TEXTURE_VERT = 
		"uniform		mat4 uMVPMatrix;" + "\n" + 
		"attribute	vec3 aPosition;" + "\n" + 
		"attribute	vec2 aTexCoord;" + "\n" + 
		"varying		vec2 vTextureCoord;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	gl_Position = uMVPMatrix * vec4(aPosition, 1);" + "\n" + 
		"	vTextureCoord = aTexCoord;" + "\n" + 
		"}" + "\n" + 
		"";

	//================ texture.frag ================
	final static String TEXTURE_FRAG = 
		"precision mediump float;" + "\n" + 
		"varying	vec2 vTextureCoord;" + "\n" + 
		"uniform	sampler2D sTexture;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	gl_FragColor = texture2D(sTexture, vTextureCoord);" + "\n" + 
		"}" + "\n" + 
		"";

	//================ texture_src_over.frag ================
	final static String TEXTURE_SRC_OVER_FRAG = 
		"precision mediump float;" + "\n" + 
		"varying	vec2 vTextureCoord;" + "\n" + 
		"uniform	sampler2D sTexture;" + "\n" + 
		"uniform	float uAlpha;" + "\n" + 
		"uniform	vec4 uSrcColor;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	//SRC_OVER [Sa + (1 - Sa)*Da, Rc = Sc + (1 - Sa)*Dc] -> D * (1 - Sa) + S" + "\n" + 
		"	gl_FragColor = texture2D(sTexture, vTextureCoord) * uAlpha + uSrcColor;" + "\n" + 
		"}" + "\n" + 
		"";

	//================ texture_dst_over.frag ================
	final static String TEXTURE_DST_OVER_FRAG = 
		"precision mediump float;" + "\n" + 
		"varying	vec2 vTextureCoord;" + "\n" + 
		"uniform	sampler2D sTexture;" + "\n" + 
		"uniform	float uAlpha;" + "\n" + 
		"uniform	vec4 uSrcColor;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	//DST_OVER [Sa + (1 - Sa)*Da, Rc = Dc + (1 - Da)*Sc] -> D + S * (1 - Da)" + "\n" + 
		"	vec4 dst = texture2D(sTexture, vTextureCoord);" + "\n" + 
		"	gl_FragColor = uSrcColor * (1.0 - dst.a) + dst * uAlpha;" + "\n" + 
		"}" + "\n" + 
		"";

	//================ texture_src_in.frag ================
	final static String TEXTURE_SRC_IN_FRAG = 
		"precision mediump float;" + "\n" + 
		"varying	vec2 vTextureCoord;" + "\n" + 
		"uniform	sampler2D sTexture;" + "\n" + 
		"uniform	vec4 uSrcColor;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	//SRC_IN [Sa * Da, Sc * Da] -> S * Da" + "\n" + 
		"	gl_FragColor = uSrcColor * texture2D(sTexture, vTextureCoord).a;" + "\n" + 
		"}" + "\n" + 
		"";

	//================ texture_dst_in.frag ================
	final static String TEXTURE_DST_IN_FRAG = 
		"precision mediump float;" + "\n" + 
		"varying	vec2 vTextureCoord;" + "\n" + 
		"uniform	sampler2D sTexture;" + "\n" + 
		"uniform	vec4 uSrcColor;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	//DST_IN [Sa * Da, Sa * Dc] -> D * Sa" + "\n" + 
		"	gl_FragColor = texture2D(sTexture, vTextureCoord) * uSrcColor.a;" + "\n" + 
		"}" + "\n" + 
		"";

	//================ texture_src_out.frag ================
	final static String TEXTURE_SRC_OUT_FRAG = 
		"precision mediump float;" + "\n" + 
		"varying	vec2 vTextureCoord;" + "\n" + 
		"uniform	sampler2D sTexture;" + "\n" + 
		"uniform	vec4 uSrcColor;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	//SRC_OUT [Sa * (1 - Da), Sc * (1 - Da)] -> S * (1 - Da)" + "\n" + 
		"	gl_FragColor = uSrcColor * (1.0 - texture2D(sTexture, vTextureCoord).a);" + "\n" + 
		"}" + "\n" + 
		"";

	//================ texture_dst_out.frag ================
	final static String TEXTURE_DST_OUT_FRAG = 
		"precision mediump float;" + "\n" + 
		"varying	vec2 vTextureCoord;" + "\n" + 
		"uniform	sampler2D sTexture;" + "\n" + 
		"uniform	float uAlpha;" + "\n" + 
		"uniform	vec4 uSrcColor;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	//DST_OUT [Da * (1 - Sa), Dc * (1 - Sa)] -> D * (1 - Sa)" + "\n" + 
		"	gl_FragColor = texture2D(sTexture, vTextureCoord) * (uAlpha - uSrcColor.a);" + "\n" + 
		"}" + "\n" + 
		"";

	//================ texture_src_atop.frag ================
	final static String TEXTURE_SRC_ATOP_FRAG = 
		"precision mediump float;" + "\n" + 
		"varying	vec2 vTextureCoord;" + "\n" + 
		"uniform	sampler2D sTexture;" + "\n" + 
		"uniform	float uAlpha;" + "\n" + 
		"uniform	vec4 uSrcColor;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	//SRC_ATOP [Da, Sc * Da + (1 - Sa) * Dc] -> D * (1 - Sa) + S * Da" + "\n" + 
		"	vec4 dst = texture2D(sTexture, vTextureCoord);" + "\n" + 
		"	gl_FragColor = dst * uAlpha + uSrcColor * dst.a;" + "\n" + 
		"}" + "\n" + 
		"";

	//================ texture_dst_atop.frag ================
	final static String TEXTURE_DST_ATOP_FRAG = 
		"precision mediump float;" + "\n" + 
		"varying	vec2 vTextureCoord;" + "\n" + 
		"uniform	sampler2D sTexture;" + "\n" + 
		"uniform	vec4 uSrcColor;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	//DST_ATOP [Sa, Sa * Dc + Sc * (1 - Da)] -> D * Sa + S * (1 - Da) " + "\n" + 
		"	vec4 dst = texture2D(sTexture, vTextureCoord);" + "\n" + 
		"	gl_FragColor = dst * uSrcColor.a + uSrcColor * (1.0 - dst.a);" + "\n" + 
		"}" + "\n" + 
		"";

	//================ texture_multiply.frag ================
	final static String TEXTURE_MULTIPLY_FRAG = 
		"precision mediump float;" + "\n" + 
		"varying	vec2 vTextureCoord;" + "\n" + 
		"uniform	sampler2D sTexture;" + "\n" + 
		"uniform	vec4 uSrcColor;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	//MULTIPLY [Sa * Da, Sc * Dc] - > D * S" + "\n" + 
		"	gl_FragColor = texture2D(sTexture, vTextureCoord) * uSrcColor;" + "\n" + 
		"}" + "\n" + 
		"";

	//================ bitmap_filter.vert ================
	final static String BITMAP_FILTER_VERT = 
		"uniform		mat4 uMVPMatrix;" + "\n" + 
		"attribute	vec3 aPosition;" + "\n" + 
		"attribute	vec2 aTexCoord;" + "\n" + 
		"attribute	vec2 aMaskTexCoord;" + "\n" + 
		"varying		vec2 vTextureCoord;" + "\n" + 
		"varying		vec2 vMaskTextureCoord;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	gl_Position = uMVPMatrix * vec4(aPosition, 1);" + "\n" + 
		"	vMaskTextureCoord = aMaskTexCoord;" + "\n" + 
		"	vTextureCoord = aTexCoord;" + "\n" + 
		"}" + "\n" + 
		"";

	//================ bitmap_filter.frag ================
	final static String BITMAP_FILTER_FRAG = 
		"precision mediump float;" + "\n" + 
		"varying	vec2 vTextureCoord;" + "\n" + 
		"uniform	sampler2D sTexture;" + "\n" + 
		"varying	vec2 vMaskTextureCoord;		//蒙板纹理坐标" + "\n" + 
		"uniform	sampler2D sTextureTemplate; //蒙板纹理" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	vec4 finalColorTemplate;" + "\n" + 
		"	vec4 finalColor;" + "\n" + 
		"	finalColor = texture2D(sTexture, vTextureCoord);" + "\n" + 
		"	finalColorTemplate= texture2D(sTextureTemplate, vMaskTextureCoord);" + "\n" + 
		"	gl_FragColor = finalColor * finalColorTemplate.a;" + "\n" + 
		"}" + "\n" + 
		"";

	//================ texture_alpha.frag ================
	final static String TEXTURE_ALPHA_FRAG = 
		"precision mediump float;" + "\n" + 
		"varying	vec2 vTextureCoord;" + "\n" + 
		"uniform	sampler2D sTexture;" + "\n" + 
		"uniform	float uAlpha;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	gl_FragColor = texture2D(sTexture, vTextureCoord) * uAlpha;" + "\n" + 
		"}           " + "\n" + 
		"";

	//================ color.vert ================
	final static String COLOR_VERT = 
		"uniform		mat4 uMVPMatrix;" + "\n" + 
		"attribute	vec3 aPosition;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	gl_Position = uMVPMatrix * vec4(aPosition, 1);" + "\n" + 
		"}" + "\n" + 
		"";

	//================ color.frag ================
	final static String COLOR_FRAG = 
		"precision mediump float;" + "\n" + 
		"uniform	vec4 uColor;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	gl_FragColor = uColor;" + "\n" + 
		"}          " + "\n" + 
		"";

	//================ color_attribute.vert ================
	final static String COLOR_ATTRIBUTE_VERT = 
		"uniform		mat4 uMVPMatrix;" + "\n" + 
		"attribute	vec3 aPosition;" + "\n" + 
		"attribute	vec4 aColor;" + "\n" + 
		"varying		vec4 vColor;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	gl_Position = uMVPMatrix * vec4(aPosition, 1);" + "\n" + 
		"	vColor = vec4(aColor.rgb * aColor.a, aColor.a);" + "\n" + 
		"}" + "\n" + 
		"";

	//================ color_attribute.frag ================
	final static String COLOR_ATTRIBUTE_FRAG = 
		"precision mediump float;" + "\n" + 
		"varying	vec4 vColor;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	gl_FragColor = vColor;" + "\n" + 
		"}" + "\n" + 
		"";
}
