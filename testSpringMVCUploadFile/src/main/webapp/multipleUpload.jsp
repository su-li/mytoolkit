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
<body>
<script src="js/jquery-3.2.1.min.js"></script>
<script type="text/javascript" color="0,0,255" opacity='0.7' zIndex="-2" count="99"
        src="http://cdn.bootcss.com/canvas-nest.js/1.0.1/canvas-nest.min.js"></script>


<script>
    var page = {
        init: function () {
            $("#upload").click($.proxy(this.upload, this));
        },
        upload: function () {
            var $files = $(":file");
            for (var j = 0; j < $files.length; ++j) {
                var $file0 = $files[j];
                $file0.append("&nbsp;<span id=\"output1\" style=\"font-size:12px\">等待</span>")
                var file = $file0.files[0],  //文件对象
                    name = file.name,        //文件名
                    size = file.size,        //总大小
                    succeed = 0;
                var shardSize = 10 * 1024 * 1024,    //以2MB为一个分片
                    shardCount = Math.ceil(size / shardSize);  //总片数
                var progress = "progress" + (j + 1);
                var outp = "output" + (j + 1);
                document.getElementById(progress).max = shardCount;
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


                    //Ajax提交

                    $.ajax({
                        url: "test/uploadFile",
                        type: "POST",
                        data: form,
                        async: true,        //异步
                        processData: false,  //很重要，告诉jquery不要对form进行处理
                        contentType: false,  //很重要，指定为false才能形成正确的Content-Type
                        success: function () {
                            ++succeed;

                            $(outp).text(succeed + " / " + shardCount);
                            document.getElementById(progress).value = succeed;
                        }
                    });
                }
            }
        }
    };

    $(function () {

        page.init();

    });

</script>
<br/><br/><br/><br/><br/><br/><br/>
<div align="center">

    <input type="file" multiple="multiple"/>&nbsp;<span id="output1" style="font-size:12px">等待</span>&nbsp;<progress
        id="progress1"></progress>
    <br/> <br/>
    <input type="file"/>&nbsp;<span id="output2" style="font-size:12px">等待</span>&nbsp;<progress
        id="progress2"></progress>
    <br/> <br/>
    <input type="file"/>&nbsp;<span id="output3" style="font-size:12px">等待</span>&nbsp;<progress
        id="progress3"></progress>
    <br/><br/><br/>

    <button id="upload">上传</button>

</div>

</body>
</html>
