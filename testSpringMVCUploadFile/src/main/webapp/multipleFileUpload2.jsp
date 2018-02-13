<%--
  Created by IntelliJ IDEA.
  User: HD
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
<script>

    var upload = function (file, md5, uploadTime, map,idx) {
        var p = 0;
        for (var key in map) {
            $("#"+idx)
            var current = map[key];
            document.getElementById(file.name).max = map.length;
            //构造一个表单，FormData是HTML5新增的
            var form = new FormData();
            form.append("data", file.slice(current.start, current.end));  //slice方法用于切出文件的一部分
            form.append("fileName", file.name);
            form.append("totalMD5", md5);
            form.append("total", map.length);  //总片数
            form.append("index", current.currentChunk);        //当前是第几片
            form.append("md5", current.md5);        //文件的MD5值
            form.append("uploadTime", uploadTime);      //文件上传时间
            //Ajax提交
            $.ajax({
                url: "test3/uploadFile",
                type: "POST",
                data: form,
                async: false,        //同步
                processData: false,  //很重要，告诉jquery不要对form进行处理
                contentType: false,  //很重要，指定为false才能形成正确的Content-Type
                success: function (data) {
                    var json = eval(data);
                    if (json.p == 1) {
                        p = 1;
                        console.log("totalSlice=" + json.total + ",currentIndex=" + json.index);
                        $("#" + file.name + "percent").text(map.length + " / " + map.length);
                        document.getElementById(file.name).value = map.length;
                    } else {
                        console.log("totalSlice=" + json.total + ",currentIndex=" + json.index);
                        $("#" + file.name + "percent").text(((document.getElementById(file.name).value) + 1) + " / " + map.length);
                        document.getElementById(file.name).value = (document.getElementById(file.name).value) + 1;
                    }
                }
            });
            if (p == 1) {
                break;
            }
        }
    };
    var generateMD5 = function (file, uploadTime, idx) {
        var map = [];
        //声明必要的变量
        var fileReader = new FileReader();
        //文件分割方法（注意兼容性）
        var blobSlice = File.prototype.mozSlice || File.prototype.webkitSlice || File.prototype.slice;
        //文件每块分割2M，计算分割详情
        var chunkSize = 2 * 1024 * 1024;
        //总片数
        var chunks = Math.ceil(file.size / chunkSize);
        //当前片数
        var currentChunk = 0;
        //创建md5对象（基于SparkMD5）
        var spark = new SparkMD5();

        //每块文件读取完毕之后的处理
        fileReader.onload = function (e) {
            console.log("读取文件", currentChunk + 1, "/", chunks);
            var data = e.target.result;
            var sparkTmp = new SparkMD5();
            sparkTmp.appendBinary(data);
            var md5Tmp = sparkTmp.end();

            var start = currentChunk * chunkSize;
            var end = Math.min(file.size, start + chunkSize);
            map.push({"currentChunk": currentChunk + 1, "md5": md5Tmp, "start": start, "end": end});

            //每块交由sparkMD5进行计算
            spark.appendBinary(e.target.result);
            currentChunk++;

            //如果文件处理完成计算MD5，如果还有分片继续处理
            if (currentChunk < chunks) {
                loadNext();
            } else {
                var flag = spark.end();
                console.log("finished loading");
                console.info("计算的Hash", flag);
                document.getElementById(file.name + "md5").innerText = "文件MD5值为: " + flag;
                upload(file, flag, uploadTime, map, idx);
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
    var listFiles = function () {
        var files = $("#ff")[0].files;
        for (var j = 0; j < files.length; j++) {
            var file = files[j];

            <!--应该用div来标识比较好,但是不会取特定子元素,先不用-->
            $("#show").append(
                "<div>" +
                "<span>文件名: " + file.name + "</span>&nbsp;&nbsp;&nbsp;" +
                "<span id=" + file.name + "md5></span>&nbsp;&nbsp;&nbsp;" +
                "<span id=" + file.name + "percent></span>&nbsp;&nbsp;&nbsp;" +
                "<progress id=" + file.name + " value=0  max =-1></progress>&nbsp;&nbsp;&nbsp;" +
                "<button id=" + j + " onchange='UP(j)'>排队中..</button></div><br/>"
            );
        }
        var UP = function (j) {
            var file = files[j];
            var uploadTime = parseInt(new Date().getTime());
            generateMD5(file, uploadTime, j);
        }
        $("#" + j).value = "0";

        <!--$("#ff").replaceWith("<input type=\"file\" id=\"ff\" multiple=\"multiple\" onchange=\"listFiles()\"/>")-->
    }

</script>


<div align="center">
    <input type="file" id="ff" multiple="multiple" onchange="listFiles()"/>
    <br/><br/><br/>
</div>
<br/><br/><br/><br/><br/><br/>
<div id="show"></div>
</body>

</html>
