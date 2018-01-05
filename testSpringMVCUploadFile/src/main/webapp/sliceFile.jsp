<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2017/12/14
  Time: 15:51
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>

<head>
    <meta charset="UTF-8">
    <title>HTML5大文件分片上传示例</title>
</head>

<body>
<script src="js/jquery-3.2.1.min.js"></script>
<script src="js/spark-md5.js"></script>
<script type="text/javascript" color="0,0,255" opacity='0.7' zIndex="-2" count="99"
        src="http://cdn.bootcss.com/canvas-nest.js/1.0.1/canvas-nest.min.js"></script>
<script>
    var page = {
        init: function () {
            $("#upload").click($.proxy(this.upload, this));
        },
        upload: function () {
            var file = $("#file")[0].files[0],  //文件对象
                name = file.name,        //文件名
                size = file.size,        //总大小
                succeed = 0;
            var shardSize = 10 * 1024 * 1024,    //以2MB为一个分片
                shardCount = Math.ceil(size / shardSize);  //总片数
            document.getElementById("progress").max = shardCount;

            for (var i = 0; i < shardCount; ++i) {
                //计算每一片的起始与结束位置
                var start = i * shardSize,
                    end = Math.min(size, start + shardSize);

                //构造一个表单，FormData是HTML5新增的

                var form = new FormData();
                form.append("data", file.slice(start, end));  //slice方法用于切出文件的一部分
                form.append("name", name);
                form.append("total", shardCount);  //总片数
                form.append("index", i + 1);        //当前是第几片
                // form.append("md5", generateMD);        //文件的MD5值


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
                        console.log("===json:fileName=" + json.fileName + ",totalSlice=" + json.totalSlice + ",currentIndex=" + json.currentIndex + ",md5=" + json.md5);
                        ++succeed;
                        $("#output").text(succeed + " / " + shardCount);
                        document.getElementById("progress").value = succeed;
                    }
                });
            }
        },
    };

    $(function () {
        page.init();
    });

</script>
<br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>
<pre id=log></pre>
<div align="center">
    <input type="file" id="file"/>
    <br/><br/><br/>
    <button id="upload">上传</button>
    <br/><br/><br/>
    <span id="output" style="font-size:12px">等待</span>
    <br/><br/><br/>
    <progress id="progress"></progress>

</div>

</body>
</html>
