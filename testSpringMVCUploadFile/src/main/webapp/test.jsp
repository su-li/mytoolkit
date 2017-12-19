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

<script>
    var lala = function () {
        $("#butt").click(function () {
            alert("Hello World click");
        });
    }
    $(function () {
        $("#flag").html("hello")
        lala()
    })

</script>

<span id="flag"></span>
<button id="butt">点击</button>

</body>
</html>
