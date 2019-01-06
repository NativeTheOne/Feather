#!/bin/bash
################################################
# 颜色输出
################################################
readonly ec=$'\033' # escape char
readonly eend=$'\033[0m' # escape end

colorEcho() {
    local color=$1
    shift

    # if stdout is console, turn on color output.
    [ -t 1 ] && echo "$ec[1;${color}m$@$eend" || echo "$@"
}

colorPrint() {
    local color=$1
    shift

    colorEcho "$color" "$@"
}

normalPrint() {
    echo "$@"
}

redPrint() {
    colorPrint 31 "$@"
}

greenPrint() {
    colorPrint 32 "$@"
}

yellowPrint() {
    colorPrint 33 "$@"
}

bluePrint() {
    colorPrint 36 "$@"
}

fatalPrint() {
    redPrint "$@"
    exit 1
}

################################################
# 控制传入的参数
################################################
while true; do
	case "$1" in
		-a)
		appName="$2"
		shift 2
		;;
		-o)
		objPut="ok"
		shift 1
		;;
		-l)
		livePut="ok"
		shift 1
		;;
		-d)
		dump="ok"
		shift 1
		;;
		-r)
		run="ok"
		shift 1
		;;
		*)
		break
		;;
	esac
done

################################################
# 输出当前操作的时间
################################################
headInfo() {
    greenPrint ================================================================================
    greenPrint "$(date "+%Y-%m-%d %H:%M:%S.%N") [$(( i + 1 ))/$update_count]: ${COMMAND_LINE[@]}"
    greenPrint ================================================================================
}

################################################
# 修改jmap输出对象分布解析为中文
################################################
SED_COMMAND_OBJ="sed -e 's/num/序号/;\
 s/instances/实例数/;\
 s/bytes/字节数/;\
 s/class name/类名/;\
 s/B/byte/;\
 s/C/char/;\
 s/D/double/;\
 s/F/float/;\
 s/I/int/;\
 s/J/long/;\
 s/Z/boolean/;\
 s/S/String/;\
'"

################################################
# 修改jmap输出堆结果解析为中文
################################################
SED_COMMAND="sed -e 's/JVM version is/JVM版本/;\
 s/Heap Configuration/堆内存初始化配置/;\
 s/MinHeapFreeRatio/JVM堆最小空闲比率(-XX:MinHeapFreeRatio)/;\
 s/MaxHeapFreeRatio/JVM堆最大空闲比率(-XX:MaxHeapFreeRatio)/;\
 s/MaxHeapSize/JVM堆的最大大小(-XX:MaxHeapSize)/;\
 s/NewSize/堆新生代的的大小(-XX:NewSize)/;\
 s/MaxNewSize/堆新生代的最大值(-XX:MaxNewSize)/;\
 s/OldSize/堆老生代的的大小(-XX:OldSize)/;\
 s/NewRatio/新生代和老年代大小比率[1:x](-XX:NewRatio)/;\
 s/SurvivorRatio/新生代中Eden区与Survivor区的大小比值[x:1:1](-XX:SurvivorRatio)/;\
 s/MetaspaceSize/分配给元空间大小(-XX:MaxMetaspaceSize)/;\
 s/MaxMetaspaceSize/分配给元空间的最大值(-XX:CompressedClassSpaceSize)/;\
 s/CompressedClassSpaceSize/类指针压缩空间大小(-XX:MaxPermSize)/;\
 s/G1HeapRegionSize/G1的Region的大小/;\
 s/Heap Usage/堆内存使用状况/;\
 s/New Generation/新生代区内存分布/;\
 s/Eden Space/Eden区内存分布/;\
 s/From Space/其中一个Survivor区的内存分布/;\
 s/To Space/另一个Survivor区的内存分布/;\
 s/tenured generation/当前老年代内存分布/'"

################################################
# 查找java环境变量（定位jmap存在）
################################################
if [ -n "$JAVA_HOME" ];then
	[ -f "$JAVA_HOME/bin/jmap" ] || fatalPrint "在java环境下找不到jmap文件"
	[ -x "$JAVA_HOME/bin/jmap" ] || fatalPrint "没有jmap的执行权限"
	jmap_path="$JAVA_HOME/bin/jmap"
elif [ -f "/usr/bin/jmap" ];then
	[ -x "/usr/bin/jmap" ] || fatalPrint "无/usr/bin/jmap的执行权限"
	jmap_path="/usr/bin/jmap"
else
	fatalPrint "系统中找不到jmap，请确保正确安装java环境"
fi

################################################
# 根据传入的appName查找用户的进程
################################################
findBusyJavaThreadsByPs() {
	if [ -n "$appName" ]; then
		pid=$( sudo ps -eo pid,cmd | grep -v grep | grep -v find-java | grep "$appName" | awk '{print $1}')
	else
		fatalPrint "请输入程序名"
	fi
}

################################################
# 使用jmap格式化输出jvm堆当前的情况
################################################
readonly run_timestamp="`date "+%Y-%m-%d_%H:%M:%S.%N"`"
readonly uuid="jmap_${run_timestamp}"

printJvmRunningStatus() {
	jmap_cmd_file="${uuid}_${pid}"
	[ -f  ${jmap_cmd_file} ] || {
	    ${jmap_path} -heap ${pid} > "$jmap_cmd_file"
		command="$SED_COMMAND  $jmap_cmd_file"
	}
}

################################################
# 使用jmap格式化输出jvm对象当前的情况
################################################
printJvmObjectStatus() {
	jmap_obj_file="${uuid}_${pid}_obj"
	[ -f ${jmap_obj_file} ] || {
		if [ -n "$objPut" ]; then {
			${jmap_path} -histo ${pid} > "$jmap_obj_file"
			obj_command="$SED_COMMAND_OBJ $jmap_obj_file"
		}
		elif [ -n "$livePut" ]; then {
			${jmap_path} -histo:live ${pid} > "$jmap_obj_file"
			obj_command="$SED_COMMAND_OBJ $jmap_obj_file"
		}
		fi		
	}
}

################################################
# 立即生成dump文件(暂不支持，因为会暂停JVM运行)
################################################
# createDumpFile() {

# }

main() {
	headInfo
	[ -n "$run" ] &&  {
		findBusyJavaThreadsByPs
		printJvmRunningStatus
		eval $command	
	}
	[ -n "$objPut" ] && {
		findBusyJavaThreadsByPs
		printJvmObjectStatus
		eval $obj_command
	}
	[ -n "$livePut" ] && {
		findBusyJavaThreadsByPs
		printJvmObjectStatus
		eval $obj_command
	}
	
}

main
