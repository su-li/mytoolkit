<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2017/12/15
  Time: 15:04
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>多个文件上传</title>
</head>
<script src="js/jquery-3.2.1.min.js"></script>
<script src="js/spark-md5.js"></script>

<body>
<script type="text/javascript" color="0,0,255" opacity='0.7' zIndex="-2" count="99"
        src="js/canvas-nest.min.js"></script>

<div align="center">
    <input type="file" id="file" multiple="multiple"/>
    <br/><br/><br/>
    <button type="button"id="butt">上传</button>
</div>
<br/><br/><br/><br/><br/><br/>
<div id="show"></div>
</body>
<script>

    var upload = function (file, md5) {
        var name = file.name,        //文件名
            size = file.size,        //总大小
            succeed = 0;
        var shardSize = 2 * 1024 * 1024,    //以2MB为一个分片
            shardCount = Math.ceil(size / shardSize);  //总片数
        document.getElementById(name).max = shardCount;

        for (var i = 0; i < shardCount; ++i) {
            //计算每一片的起始与结束位置
            var start = i * shardSize,
                end = Math.min(size, start + shardSize);

            //构造一个表单，FormData是HTML5新增的

            var form = new FormData();
            form.append("data", file.slice(start, end));  //slice方法用于切出文件的一部分
            form.append("fileName", name);
            form.append("total", shardCount);  //总片数
            form.append("index", i + 1);        //当前是第几片
            form.append("md5", md5);        //文件的MD5值

            //Ajax提交

            $.ajax({
                url: "test/uploadFile",
                type: "POST",
                data: form,
                async: true,        //异步
                processData: false,  //很重要，告诉jquery不要对form进行处理
                contentType: false,  //很重要，指定为false才能形成正确的Content-Type
                success: function (data) {
                    var json = eval(data);
                    console.log("fileName=" + json.fileName);
                    var pro = document.getElementById(json.fileName);
                    pro.value = pro.value + 1;
                    document.getElementById(json.fileName + "percent").innerText = pro.value + "/" + pro.max;
                }
            });
        }
    };
    var generateMD5 = function (file) {
        //声明必要的变量
        var fileReader = new FileReader();//box = document.getElementById('box');
        //文件分割方法（注意兼容性）
      var   blobSlice = File.prototype.mozSlice || File.prototype.webkitSlice || File.prototype.slice,
            //文件每块分割2M，计算分割详情
            chunkSize = 2097152,
            chunks = Math.ceil(file.size / chunkSize),
            currentChunk = 0,
            //创建md5对象（基于SparkMD5）
            spark = new SparkMD5();

        //每块文件读取完毕之后的处理
        fileReader.onload = function (e) {
            console.log("读取文件", currentChunk + 1, "/", chunks);
            //每块交由sparkMD5进行计算
            spark.appendBinary(e.target.result);
            currentChunk++;

            //如果文件处理完成计算MD5，如果还有分片继续处理
            if (currentChunk < chunks) {
                loadNext();
            } else {
                // box.innerText = 'MD5 hash:' + spark.end();
                var flag = spark.end();
                console.log("finished loading");
                console.info("计算的Hash", flag);
                document.getElementById(file.name + "md5").innerText = "md5值为:" + flag;
                upload(file, flag)

            }
        };

        //处理单片文件的上传
        function loadNext() {
            var start = currentChunk * chunkSize,
                end = start + chunkSize >= file.size ? file.size : start + chunkSize;

            fileReader.readAsBinaryString(blobSlice.call(file, start, end));
        }

        loadNext();
    };
    $("#butt").click(function () {
        var files = $("#file")[0].files;
        for (var j = 0; j < files.length; j++) {
            var file = files[j];
            $("#show").append("<span>文件名: " + file.name + "</span>&nbsp;&nbsp;&nbsp;" +
                "<span id=" + file.name + "md5></span>&nbsp;&nbsp;&nbsp;" +
                "<span id=" + file.name + "percent></span>&nbsp;&nbsp;&nbsp;" +
                "<progress id=" + file.name + " value=" + 0 + "></progress><br/>");
            generateMD5(file);
        }
    })
</script>
</html>
