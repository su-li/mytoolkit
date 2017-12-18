<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2017/12/15
  Time: 17:52
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<script src="js/jquery-3.2.1.min.js"></script>
<script src="js/spark-md5.min.js"></script>
<script type="text/javascript" color="0,0,255" opacity='0.7' zIndex="-2" count="99"
        src="http://cdn.bootcss.com/canvas-nest.js/1.0.1/canvas-nest.min.js"></script>

<input type="file" id="file">
<button id="tt">MD5</button>
<script>
    generateMD5 = function (file) {
        var blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice,
            chunkSize = 2097152, // read in chunks of 2MB
            chunks = Math.ceil(file.size / chunkSize),
            currentChunk = 0,
            spark = new SparkMD5.ArrayBuffer(),
            frOnload = function (e) {
                //  log.innerHTML+="\nread chunk number "+parseInt(currentChunk+1)+" of "+chunks;
                spark.append(e.target.result); // append array buffer
                currentChunk++;
                if (currentChunk < chunks)
                    loadNext();
                else
                    alert(spark.end())
                log.innerHTML += "\n加载结束 :\n\n计算后的文件md5:\n" + spark.end() + "\n\n现在你可以选择另外一个文件!\n";
            },
            frOnerror = function () {
                log.innerHTML += "\糟糕，好像哪里错了.";
            };

        function loadNext() {
            var fileReader = new FileReader();
            fileReader.onload = frOnload;
            fileReader.onerror = frOnerror;
            var start = currentChunk * chunkSize,
                end = ((start + chunkSize) >= file.size) ? file.size : start + chunkSize;
            fileReader.readAsArrayBuffer(blobSlice.call(file, start, end));
        };

        loadNext();
    };
    var filee = $("#file")[0].files[0];
    $("#tt").click(generateMD5(filee));



</script>
</body>
</html>
