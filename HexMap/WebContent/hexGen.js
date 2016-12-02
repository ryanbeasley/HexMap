/**
 * 
 */

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
	/*
	shaderProgram.textureCoordAttribute = gl.getAttribLocation(shaderProgram, "aTextureCoord");
	gl.enableVertexAttribArray(shaderProgram.textureCoordAttribute);
	*/
	shaderProgram.pMatrixUniform = gl.getUniformLocation(shaderProgram, "uPMatrix");
	shaderProgram.mvMatrixUniform = gl.getUniformLocation(shaderProgram, "uMVMatrix");
	shaderProgram.nMatrixUniform = gl.getUniformLocation(shaderProgram, "uNMatrix");
	
	/*
	shaderProgram.samplerGrassUniform = gl.getUniformLocation(shaderProgram, "uSamplerGrass");
	shaderProgram.samplerWaterUniform = gl.getUniformLocation(shaderProgram, "uSamplerWater");
	shaderProgram.samplerCoastUniform = gl.getUniformLocation(shaderProgram, "uSamplerCoast");
	
	shaderProgram.ambientColorUniform = gl.getUniformLocation(shaderProgram, "uAmbientColor");
	shaderProgram.materialShininessUniform = gl.getUniformLocation(shaderProgram, "uMaterialShininess");
    shaderProgram.pointLightingLocationUniform = gl.getUniformLocation(shaderProgram, "uPointLightingLocation");
    shaderProgram.pointLightingSpecularColorUniform = gl.getUniformLocation(shaderProgram, "uPointLightingSpecularColor");
    shaderProgram.pointLightingDiffuseColorUniform = gl.getUniformLocation(shaderProgram, "uPointLightingDiffuseColor");
    */
}

/*
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
}

var grassTexture;
var waterTexture;
var mountainsTexture;
var snowyMountaintsTexture;
var coastTexture;
var forestTexture;

function initTexture(){
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
	
	
	forestTexture = gl.createTexture();
	forestTexture.image = new Image();
	forestTexture.image.onload = function(){
		handleLoadedTexture(forestTexture)
	}
	forestTexture.image.src = "forest.jpg";
	
	snowyMountainsTexture = gl.createTexture();
	snowyMountainsTexture.image = new Image();
	snowyMountainsTexture.image.onload = function(){
		handleLoadedTexture(snowyMountainsTexture)
	}
	snowyMountainsTexture.image.src = "snowyMountains.jpg";
	
	mountainsTexture = gl.createTexture();
	mountainsTexture.image = new Image();
	mountainsTexture.image.onload = function(){
		handleLoadedTexture(mountainsTexture)
	}
	mountainsTexture.image.src = "mountains.jpg";
	
}
*/
var mvMatrix = mat4.create();
var mvMatrixStack = [];
var pMatrix = mat4.create();

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
	gl.bufferData(gl.ARRAY_BUFFER, new Uint16Array(mapData.biomeTypes), gl.STATIC_DRAW);
	mapBiomeTypeBuffer.itemSize = 1;
	mapBiomeTypeBuffer.numItems = mapBiomeTypeBuffer.length;
	
	indexBuffer = gl.createBuffer();
	gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, indexBuffer);
	gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(mapData.indices), gl.STATIC_DRAW);
	indexBuffer.itemSize = 1;
	indexBuffer.numItems = indexBuffer.length;
	/*
	texCoordBuffer = gl.createBuffer();
	gl.bindBuffer(gl.ARRAY_BUFFER, texCoordBuffer);
	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(mapData.textCoord), gl.STATIC_DRAW);
	texCoordBuffer.itemSize = 2;
	texCoordBuffer.numItems = texCoordBuffer.length / 2;
	*/
}

function loadMap() {
	var request = new XMLHttpRequest();
	request.open("GET", "test.json");
	request.onreadystatechange = function () {
		if (request.readyState == 4) {
			handleLoadedHexMap(JSON.parse(request.responseText));
		}
	}
	request.sent();
}

var zoom = 0;
var yrot = 0;
var xrot = 0;
var xRot = 0;
var yRot = 0;

function drawScene(){
	requestAnimationFrame(drawScene);
	if (!flag) return;
	
	gl.viewport(0, 0, gl.viewportWidth, glviewportHeight);
	gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
	
	if (mapVertexPositionBuffer == null || mapVertexNormalBuffer == null || mapBiomeTypeBuffer == null){
		return;
	}
	
	mat4.perspective(pMatrix, Math.PI / 4, gl.viewportWidth / gl.viewportHeight, 0.1, 100.0);
	
	mat4.identity(mvMatrix);
	
	mat4.translate(mvMatrix, mvMatrix, [0, 0, -10, 0]);
	//mat4.rotate(mvMatrix, mvMatrix, degToRad(yrot), [0, 1, 0]);
	//mat4.rotate(mvMatrix, mvMatrix, degToRad(xrot), [1, 0, 0]);
	/*
	gl.bindTexture(gl.TEXTURE_2D, grassTexture);
	gl.uniform1i(shaderProgram.samplerGrassUniform);
	
	gl.bindTexture(gl.TEXTURE_2D, waterTexture);
	gl.uniform1i(shaderProgram.samplerWaterUniform);
	
	gl.bindTexture(gl.TEXTURE_2D, coastTexture);
	gl.uniform1i(shaderProgram.samplerCoastUniform);
	*/
	gl.bindBuffer(gl.ARRAY_BUFFER, mapVertexPositionBuffer);
	gl.vertexAttribPointer(shaderProgram.vertexPositionAttribute, mapVertexPositionBuffer.itemSize, gl.FLOAT, false, 0, 0);
	
	gl.bindBuffer(gl.ARRAY_BUFFER, mapVertexNormalBuffer);
	gl.vertexAttribPointer(shaderProgram.vertexNormalAttribute, mapVertexNormalBuffer.itemSize, gl.FLOAT, false, 0, 0);
	
	gl.bindBuffer(gl.ARRAY_BUFFER, mapBiomeTypeBuffer);
	gl.vertexAttribPointer(shaderProgram.biomeTypeAttribute, mapBiomeTypeBuffer.itemSize, gl.UNSIGNED_SHORT, false, 0, 0);
	/*
	gl.bindBuffer(gl.ARRAY_BUFFER, texCoordBuffer);
	gl.vertexAttribPointer(shaderProgram.textureCoordAttribute, texCoordBuffer.itemSize, gl.FLOAT, false, 0, 0);
	*/
	gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, indexBuffer);
	setMatrixUniforms();
	gl.drawElements(gl.TRIANGLES, indexBuffer.numItems, gl.UNSIGNED_SHORT, 0);
}

var currentlyPressedKeys = {};

function handleKeyDown(event){
	currentlyPressedKeys[even.keyCode] = true;	
}

function handleKeyUp(event){	
	currentlyPressedKeys[event.keyCode] = false;
	xrot = 0;
	yrot = 0;
}

function handleKeys(){
	if (currentlyPressedKeys[33]){
		z -= 0.05;
	}
	if (currentlyPressedKeys[34]){
		z += 0.05;
	}
	if (currentlyPressedKeys[37]){
		yRot -= 1;
	}
	if (currentlyPressedKeys[39]){
		yrot += 1;
	}
	if (currentlyPressedKeys[38]){
		xrot -= 1;
	}
	if (currentlyPressedKeys[40]){
		xrot += 1;
	}
}

function animate(){
	var timeNow = new Date().getTime();
	if (lastTime != 0){
		var elapsed = timeNow - lastTime;
		
		xRot += (xrot * elapsed) / 1000.0;
		yRot += (yrot * elapsed) / 1000.0;
	}
	lastTime = timeNow;
}

function tick(){
	requestAnimFrame(tick);
	handleKeys;
	drawScene();
	animate();
}

function webGLStart(){
	var canvas = document.getElementById("HexMapGenerator");
	initGL(canvas);
	initShaders();
	//initTextures();
	loadMap();
	
	gl.clearColor(0.0, 0.0, 0.0, 1.0);
	gl.enable(gl.DEPTH_TEST);
	
	document.onkeydown = handleKeyDown;
	document.onkeyup = handleKeyUp;
	
	tick();
}