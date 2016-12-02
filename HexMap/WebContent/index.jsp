<%@ page import="HexGrid.*" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>HexMapGenerator</title>
<%-- 

 --%>
<script type="text/javascript" src="gl-matrix.js"></script>
<script type="text/javascript" src="webgl-utils.js"></script>
<script id="shader-fs" type="x-shader/x-fragment">
	precision mediump float;

	varying vec3 vTransformedNormal;
	varying vec4 vPosition;
	varying vec3 vType;
	varying vec2 vTextureCoord;
	varying float vNoise;

	uniform float uMaterialShininess;
    uniform vec3 uAmbientColor;
    uniform vec3 uPointLightingLocation;
    uniform vec3 uPointLightingSpecularColor;
    uniform vec3 uPointLightingDiffuseColor;


	uniform sampler2D uSamplerGrass;
	uniform sampler2D uSamplerWater;
	uniform sampler2D uSamplerCoast;
	uniform sampler2D uSamplerSnowyMountains;

	void main(void){
		vec3 color = vec3(0.0, 0.0, 0.0);
		vec4 land = texture2D(uSamplerGrass, vec2(vTextureCoord.s, vTextureCoord.t)); // biome 1 green
		vec4 water = texture2D(uSamplerWater, vec2(vTextureCoord.s, vTextureCoord.t)); // biome 2 blue
		vec4 coast = texture2D(uSamplerCoast, vec2(vTextureCoord.s, vTextureCoord.t)); // biome 3 yellow
		vec4 mountain = texture2D(uSamplerSnowyMountains, vec2(vTextureCoord.s, vTextureCoord.t)); //biome 4 mountain white

		// assign biome

		if (abs(vType.x - 1.0) < .1){
			color = color + .3 * land.rgb;
		}
		else if (abs(vType.x - 2.0) < .1){
			color = color + .3 * water.rgb;
		}
		else if (abs(vType.x - 3.0) < .1){
			color = color + .3 * coast.rgb;
		}
		else {
			color = color + .3 * mountain.rgb;
		}

		if (abs(vType.y - 1.0) < .1){
			color = color + .3 * land.rgb;
		}
		else if (abs(vType.y - 2.0) < .1){
			color = color + .3 * water.rgb;
		}
		else if (abs(vType.y - 3.0) < .1){
			color = color + .3 * coast.rgb;
		}
		else {
			color = color + .3 * mountain.rgb;
		}

		if (abs(vType.z - 1.0) < .1){
			color = color + .3 * land.rgb;
		}
		else if (abs(vType.z - 2.0) < .1){
			color = color + .3 * water.rgb;
		}
		else if (abs(vType.z - 3.0) < .1){
			color = color + .3 * coast.rgb;
		}
		else {
			color = color + .3 * mountain.rgb;
		}

		// add noise
		color = color * vNoise;
		// light shader
		
		vec3 lightDirection = normalize(uPointLightingLocation - vPosition.xyz);
		vec3 normal = normalize(vTransformedNormal);		
		
		// specular
		vec3 eyeDirection = normalize(-vPosition.xyz);
		vec3 reflectionDirection = reflect(-lightDirection,normal);

		float specularLightWeighting = pow(max(dot(reflectionDirection, eyeDirection), 0.0), uMaterialShininess);

		// diffuse
		float diffuseLightWeighting = max(dot(normal, lightDirection), 0.0);
		vec3 lightWeighting = uAmbientColor
			+ uPointLightingSpecularColor * specularLightWeighting
            + uPointLightingDiffuseColor * diffuseLightWeighting;

		gl_FragColor = vec4(color * lightWeighting, 1.0);
	}
</script>
<script id="shader-vs" type="x-shader/x-vertex">
	attribute vec3 aVertexPosition;
	attribute vec3 aVertexNormal;
	attribute vec3 aBiomeType;
	attribute vec2 aTextureCoord;	
	attribute float aNoise;

	uniform mat4 uMVMatrix;
	uniform mat4 uPMatrix;
	uniform mat3 uNMatrix;

	varying vec3 vTransformedNormal;
	varying vec4 vPosition;
	varying vec3 vType;
	varying vec2 vTextureCoord;
	varying float vNoise;

	void main(void){
		vPosition = uMVMatrix * vec4(aVertexPosition, 1.0);
		gl_Position = uPMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);
		vTransformedNormal = uNMatrix * aVertexNormal;
		vType = aBiomeType;
		vTextureCoord = aTextureCoord;
		vNoise = aNoise;
	}
</script>
<script type="text/javascript">
var gl;
function initGL(canvas){
	try{
		gl = canvas.getContext("experimental-webgl");
		gl.viewportWidth = canvas.width;
		gl.viewportHeight = canvas.height;
	} catch(e){
		
	}
	
	if (!gl){
		alert("Could not initialise WebGL, sorry.");
	}	
}

function getShader(gl, id){
	var shaderScript = document.getElementById(id);
	if (!shaderScript) {
		return null;
	}
	
	var str = "";
	var k = shaderScript.firstChild;
	while(k){
		if (k.nodeType == 3){
			str += k.textContent;
		}
		k = k.nextSibling;
	}
	
	var shader;
	if (shaderScript.type == "x-shader/x-fragment"){
		shader = gl.createShader(gl.FRAGMENT_SHADER);
	}
	else if (shaderScript.type == "x-shader/x-vertex"){
		shader = gl.createShader(gl.VERTEX_SHADER);
	}
	else {
		return null;
	}
	
	gl.shaderSource(shader, str);
	gl.compileShader(shader);
	
	if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)){
		alert(gl.getShaderInfoLog(shader));
		return null;
	}
	
	return shader;
}

var shaderProgram;

function initShaders(){
	var fragmentShader = getShader(gl, "shader-fs");
	var vertexShader = getShader(gl, "shader-vs");
	
	shaderProgram = gl.createProgram();
	gl.attachShader(shaderProgram, vertexShader);
	gl.attachShader(shaderProgram, fragmentShader);
	gl.linkProgram(shaderProgram);
	
	if (!gl.getProgramParameter(shaderProgram, gl.LINK_STATUS)){
		alert("Could not initialise shaders");
	}
	
	gl.useProgram(shaderProgram);
	
	shaderProgram.vertexPositionAttribute = gl.getAttribLocation(shaderProgram, "aVertexPosition");
	gl.enableVertexAttribArray(shaderProgram.vertexPositionAttribute);
	
	shaderProgram.vertexNormalAttribute = gl.getAttribLocation(shaderProgram, "aVertexNormal");
	gl.enableVertexAttribArray(shaderProgram.vertexNormalAttribute);
	
	shaderProgram.biomeTypeAttribute = gl.getAttribLocation(shaderProgram, "aBiomeType");
	gl.enableVertexAttribArray(shaderProgram.biomeTypeAttribute);
	
	shaderProgram.textureCoordAttribute = gl.getAttribLocation(shaderProgram, "aTextureCoord");
	gl.enableVertexAttribArray(shaderProgram.textureCoordAttribute);
	
	shaderProgram.noiseAttribute = gl.getAttribLocation(shaderProgram, "aNoise");
	gl.enableVertexAttribArray(shaderProgram.noiseAttribute);
	
	shaderProgram.pMatrixUniform = gl.getUniformLocation(shaderProgram, "uPMatrix");
	shaderProgram.mvMatrixUniform = gl.getUniformLocation(shaderProgram, "uMVMatrix");
	shaderProgram.nMatrixUniform = gl.getUniformLocation(shaderProgram, "uNMatrix");
	
	
	shaderProgram.samplerGrassUniform = gl.getUniformLocation(shaderProgram, "uSamplerGrass");
	shaderProgram.samplerWaterUniform = gl.getUniformLocation(shaderProgram, "uSamplerWater");
	shaderProgram.samplerCoastUniform = gl.getUniformLocation(shaderProgram, "uSamplerCoast");
	shaderProgram.samplerSnowyMountainsUniform = gl.getUniformLocation(shaderProgram, "uSamplerSnowyMountains");
	
	
	
	shaderProgram.ambientColorUniform = gl.getUniformLocation(shaderProgram, "uAmbientColor");
	shaderProgram.materialShininessUniform = gl.getUniformLocation(shaderProgram, "uMaterialShininess");
    shaderProgram.pointLightingLocationUniform = gl.getUniformLocation(shaderProgram, "uPointLightingLocation");
    shaderProgram.pointLightingSpecularColorUniform = gl.getUniformLocation(shaderProgram, "uPointLightingSpecularColor");
    shaderProgram.pointLightingDiffuseColorUniform = gl.getUniformLocation(shaderProgram, "uPointLightingDiffuseColor");
    
}
var countText = 0;

function handleLoadedTexture(texture){
	gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, true);
	
	gl.bindTexture(gl.TEXTURE_2D, texture);
	gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, true);
	gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, texture.image);
	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR_MIPMAP_LINEAR);
	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.REPEAT);
	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.REPEAT);
	gl.generateMipmap(gl.TEXTURE_2D);
	
	gl.bindTexture(gl.TEXTURE_2D, null);
	countText++;
	
	if (countText == 4){
		drawScene();
	}
}

var grassTexture;
var waterTexture;
var mountainsTexture;
var snowyMountaintsTexture;
var coastTexture;
var forestTexture;
var tflag = false;

function initTextures(){
	grassTexture = gl.createTexture();
	grassTexture.image = new Image();
	grassTexture.image.onload = function(){
		handleLoadedTexture(grassTexture)
	}
	grassTexture.image.src = "grassland.jpg";
	
	waterTexture = gl.createTexture();
	waterTexture.image = new Image();
	waterTexture.image.onload = function(){
		handleLoadedTexture(waterTexture)
	}
	waterTexture.image.src = "water.jpg";
	
	coastTexture = gl.createTexture();
	coastTexture.image = new Image();
	coastTexture.image.onload = function(){
		handleLoadedTexture(coastTexture)
	}
	coastTexture.image.src = "coast.jpg";
	
	/*
	forestTexture = gl.createTexture();
	forestTexture.image = new Image();
	forestTexture.image.onload = function(){
		handleLoadedTexture(forestTexture)
	}
	forestTexture.image.src = "forest.jpg";
	*/
	snowyMountainsTexture = gl.createTexture();
	snowyMountainsTexture.image = new Image();
	snowyMountainsTexture.image.onload = function(){
		handleLoadedTexture(snowyMountainsTexture)
	}
	snowyMountainsTexture.image.src = "snowyMountains.jpg";
	/*
	mountainsTexture = gl.createTexture();
	mountainsTexture.image = new Image();
	mountainsTexture.image.onload = function(){
		handleLoadedTexture(mountainsTexture)
	}
	mountainsTexture.image.src = "mountains.jpg";
	*/
}

var mvMatrix = mat4.create();
//var mvMatrixStack = [];
var pMatrix = mat4.create();
/*
function mvPushMatrix(){
	var copy = mat4.create();
	mat4.set(mvMatrix, copy);
	mvMatrixStack.push(copy);
}

function mvPopMatrix(){
	if (mvMatrixStack.length == 0){
		throw "Invalid popMatrix!";
	}
	mvMatrix = mvMatrixStack.pop();
}
*/
function setMatrixUniforms(){
	gl.uniformMatrix4fv(shaderProgram.pMatrixUniform, false, pMatrix);
	gl.uniformMatrix4fv(shaderProgram.mvMatrixUniform, false, mvMatrix);
	
	var normalMatrix = mat3.create();
	mat4.toInverseMat3(mvMatrix, normalMatrix);
	mat3.transpose(normalMatrix, normalMatrix);
	
}

function degToRad(degrees){
	return degrees * Math.PI / 180;
}

var mapVertexPositionBuffer;
var mapVertexNormalBuffer;
var mapBiomeTypeBuffer;
var indexBuffer;
var texCoordBuffer;
var noiseBuffer;

var flag = false;

function handleLoadedHexMap(mapData){
	mapVertexPositionBuffer = gl.createBuffer();
	gl.bindBuffer(gl.ARRAY_BUFFER, mapVertexPositionBuffer);
	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(mapData.vertexPositions), gl.STATIC_DRAW);
	mapVertexPositionBuffer.itemSize = 3;
	mapVertexPositionBuffer.numItems = mapData.vertexPositions.length / 3;
	
	mapVertexNormalBuffer = gl.createBuffer();
	gl.bindBuffer(gl.ARRAY_BUFFER, mapVertexNormalBuffer);
	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(mapData.vertexNormals), gl.STATIC_DRAW);
	mapVertexNormalBuffer.itemSize = 3;
	mapVertexNormalBuffer.numItems = mapData.vertexNormals.length / 3;
	
	mapBiomeTypeBuffer = gl.createBuffer();
	gl.bindBuffer(gl.ARRAY_BUFFER, mapBiomeTypeBuffer);
	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(mapData.biomeTypes), gl.STATIC_DRAW);
	mapBiomeTypeBuffer.itemSize = 3;
	mapBiomeTypeBuffer.numItems = mapData.biomeTypes.length / 3;
	
	indexBuffer = gl.createBuffer();
	gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, indexBuffer);
	gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(mapData.indices), gl.STATIC_DRAW);
	indexBuffer.itemSize = 1;
	indexBuffer.numItems = mapData.indices.length;
	
	texCoordBuffer = gl.createBuffer();
	gl.bindBuffer(gl.ARRAY_BUFFER, texCoordBuffer);
	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(mapData.textCoord), gl.STATIC_DRAW);
	texCoordBuffer.itemSize = 2;
	texCoordBuffer.numItems = texCoordBuffer.length / 2;
	
	noiseBuffer = gl.createBuffer();
	gl.bindBuffer(gl.ARRAY_BUFFER, noiseBuffer);
	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(mapData.noise), gl.STATIC_DRAW);
	noiseBuffer.itemSize = 1;
	noiseBuffer.numItems = mapData.noise.length;
	
	initTextures();
}

function loadMap() {
	var request = new XMLHttpRequest();
	request.open("GET", "test.json");
	request.onreadystatechange = function () {
		if (request.readyState == 4) {
			handleLoadedHexMap(JSON.parse(request.responseText));
		}
	}
	request.send();
}

var z = -150;
var yrot = 0;
var xrot = 0;
var xRot = 0;
var yRot = 45;
var zRot = 0;

function drawScene(){	
	gl.viewport(0, 0, gl.viewportWidth, gl.viewportHeight);
	gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
	
	//set uniforms
	gl.uniform3f(
                shaderProgram.ambientColorUniform,
                .5,
                .5,
                .5
            );
	
	gl.uniform3f(
            shaderProgram.pointLightingLocationUniform,
            -120,
            -60,
            100
        );
	
	gl.uniform3f(
			shaderProgram.pointLightingSpecularColorUniform,
            1,
            1,
            1
        );
	
	gl.uniform3f(
            shaderProgram.pointLightingDiffuseColorUniform,
            1,
            1,
            1
        );
	
	gl.uniform1f(shaderProgram.materialShininessUniform, 10);
	
	//set matrix
	
	mat4.perspective(pMatrix, degToRad(45), gl.viewportWidth / gl.viewportHeight, 0.1, 1000.0);
	
	mat4.identity(mvMatrix);
	
	mat4.translate(mvMatrix, mvMatrix, [-80, -40, z]);
	mat4.rotate(mvMatrix, mvMatrix, degToRad(yRot), [-1, 0, 0]);
	mat4.rotate(mvMatrix, mvMatrix, degToRad(xRot), [0, -1, 0]);
	mat4.rotate(mvMatrix, mvMatrix, degToRad(zRot), [0, 0, -1]);
	
	
	
	gl.bindTexture(gl.TEXTURE_2D, grassTexture);
	gl.uniform1i(shaderProgram.samplerGrassUniform, 0);

	gl.bindTexture(gl.TEXTURE_2D, waterTexture);
	gl.uniform1i(shaderProgram.samplerWaterUniform, 1);
	
	gl.bindTexture(gl.TEXTURE_2D, coastTexture);
	gl.uniform1i(shaderProgram.samplerCoastUniform, 2);
	
	gl.bindTexture(gl.TEXTURE_2D, snowyMountaintsTexture);
	gl.uniform1i(shaderProgram.samplerSnowyMountainsUniform, 2);
	
	gl.bindBuffer(gl.ARRAY_BUFFER, mapVertexPositionBuffer);
	gl.vertexAttribPointer(shaderProgram.vertexPositionAttribute, mapVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);
	
	gl.bindBuffer(gl.ARRAY_BUFFER, mapVertexNormalBuffer);
	gl.vertexAttribPointer(shaderProgram.vertexNormalAttribute, mapVertexNormalBuffer.itemSize, gl.FLOAT, false, 0, 0);

	gl.bindBuffer(gl.ARRAY_BUFFER, mapBiomeTypeBuffer);
	gl.vertexAttribPointer(shaderProgram.biomeTypeAttribute, mapBiomeTypeBuffer.itemSize, gl.FLOAT, false, 0, 0);
	
	gl.bindBuffer(gl.ARRAY_BUFFER, texCoordBuffer);
	gl.vertexAttribPointer(shaderProgram.textureCoordAttribute, texCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);
	
	gl.bindBuffer(gl.ARRAY_BUFFER, noiseBuffer);
	gl.vertexAttribPointer(shaderProgram.noiseAttribute, noiseBuffer.itemSize, gl.FLOAT, false, 0, 0);
	
	gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, indexBuffer);
	setMatrixUniforms();
	gl.drawElements(gl.TRIANGLES, indexBuffer.numItems, gl.UNSIGNED_SHORT, 0);
}

function webGLStart(){
	var canvas = document.getElementById("HexMapGenerator");
	initGL(canvas);
	initShaders();
	
	gl.clearColor(0.0, 0.0, 0.0, 1.0);
	gl.enable(gl.DEPTH_TEST);
	
	loadMap();
}
</script>
</head>
<body>
<%= Controller.createHexMesh() %>   

<input type="button" value="Load Map" id="loadMap" onclick="webGLStart();"/>
<canvas id="HexMapGenerator" style="border: none;" width="2000" height="1000"></canvas>
</body>
</html>