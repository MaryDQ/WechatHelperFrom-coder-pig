package com.coderpig.wechathelper

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.PendingIntent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.orhanobut.hawk.Hawk


/**
 * 描述：无障碍服务类
 *
 * @author CoderPig on 2018/04/12 13:47.
 */
class HelperService : AccessibilityService() {

    private val TAG = "HelperService"
    private val handler = Handler()
    private var curGroup = ""
    private var mMember = Member()

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val eventType = event.eventType
        val classNameChr = event.className
        val className = classNameChr.toString()
        Log.d(TAG, event.toString())
        when (eventType) {
            AccessibilityEvent.TYPE_VIEW_SCROLLED->{
                if (event.packageName=="com.tencent.mm") {
                    openRedPacket()
                }else{
                    //doNothing
                }

            }

            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                when (className) {
                    "com.tencent.mm.ui.LauncherUI" -> openRedPacket()
                    "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI" -> clickRedPacket()
                    "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI" -> performBackClick()
                }

                if (Hawk.get(Constant.ADD_FRIENDS, false)) {
                    when (className) {
                        "com.tencent.mm.ui.LauncherUI" -> {
//                            openGroup()
                        }
                        "com.tencent.mm.ui.contact.ChatroomContactUI" -> searchGroup()
                        "com.tencent.mm.ui.chatting.ChattingUI" -> openGroupSetting()
                        "com.tencent.mm.chatroom.ui.ChatroomInfoUI" -> openSelectContact()
                        "com.tencent.mm.ui.contact.SelectContactUI" -> addMembers()
                    }
                }
                if (className == "com.tencent.mm.ui.widget.a.c") {
                    dialogClick()
                }
                if (Hawk.get(Constant.FRIEND_SQUARE, false)) {
                    if (className == "com.tencent.mm.plugin.sns.ui.SnsTimeLineUI") {
                        autoZan()
                    }
                }
            }

            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                if (event.parcelableData != null && event.parcelableData is Notification) {
                    val notification = event.parcelableData as Notification
                    val content = notification.tickerText.toString()
                    Log.e("通知11111", content)
                    if (content.contains("[微信红包]")) {
                        val pendingIntent = notification.contentIntent
                        try {
                            pendingIntent.send()
                        } catch (e: PendingIntent.CanceledException) {
                            e.printStackTrace()
                        }

                    } else if (content.contains("dingding")) {
                        val intent = packageManager.getLaunchIntentForPackage("com.alibaba.android.rimet")
                        startActivity(intent)
                    }
                }
            }

            //            //滚动的时候也去监听红包，不过有点卡
////            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
////                if (className == "android.widget.ListView") {
////                    openRedPacket()
////                }
////            }
//        }
//    }
        }
    }

    //1.打开群聊
    private fun openGroup() {
        mMember = Hawk.get<Member>(Constant.MEMBER)
        if (mMember.python_1.size != 0 || mMember.android.size != 0 || mMember.python_2.size != 0 || mMember.guy.size != 0) {
            curGroup = when {
                mMember.python_1.size > 0 -> Constant.GROUP_NAME_1
                mMember.python_2.size > 0 -> Constant.GROUP_NAME_2
                mMember.android.size > 0 -> Constant.GROUP_NAME_3
                mMember.guy.size > 0 -> Constant.GROUP_NAME_5
                else -> ""
            }
            val nodeInfo = rootInActiveWindow
            if (nodeInfo != null) {
                val tabNodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/d9a")
                for (tabNode in tabNodes) {
                    if (tabNode.text.toString() == "通讯录") {
                        tabNode.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        handler.postDelayed({
                            val newNodeInfo = rootInActiveWindow
                            if (newNodeInfo != null) {
                                val tagNodes = newNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ng")
                                for (tagNode in tagNodes) {
                                    if (tagNode.text.toString() == "群聊") {
                                        tagNode.parent.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                        break
                                    }
                                }
                            }
                        }, 500L)
                    }
                }
            }
        }
    }

    //2.搜索群聊
    private fun searchGroup() {
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            val nodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/nr")
            for (info in nodes) {
                if (info.text.toString() == curGroup) {
                    info.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    break
                }
            }
        }
    }

    //3.打开群聊设置
    private fun openGroupSetting() {
        when (curGroup) {
            Constant.GROUP_NAME_1 -> {
                if (mMember.python_1.size > 0) {
                    val nodeInfo = rootInActiveWindow
                    if (nodeInfo != null) {
                        nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/kj")[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
            }
            Constant.GROUP_NAME_2 -> {
                if (mMember.python_2.size > 0) {
                    val nodeInfo = rootInActiveWindow
                    if (nodeInfo != null) {
                        nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/kj")[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
            }
            Constant.GROUP_NAME_3 -> {
                if (mMember.android.size > 0) {
                    val nodeInfo = rootInActiveWindow
                    if (nodeInfo != null) {
                        nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/kj")[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
            }
            Constant.GROUP_NAME_5 -> {
                if (mMember.guy.size > 0) {
                    val nodeInfo = rootInActiveWindow
                    if (nodeInfo != null) {
                        nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/kj")[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
            }
            else -> {
                performBackClick()
            }
        }
    }

    //4.滚动后点击添加按钮，打开添加成员页面
    private fun openSelectContact() {

        if (curGroup != "") {
            var members = arrayListOf<String>()
            when (curGroup) {
                Constant.GROUP_NAME_1 -> members = mMember.python_1
                Constant.GROUP_NAME_2 -> members = mMember.python_2
                Constant.GROUP_NAME_3 -> members = mMember.android
                Constant.GROUP_NAME_5 -> members = mMember.guy
            }
            if (members.size > 0) {
                val nodeInfo = rootInActiveWindow
                if (nodeInfo != null) {
                    val numText = nodeInfo.findAccessibilityNodeInfosByViewId("android:id/text1")[0].text.toString()
                    val memberCount = numText.substring(numText.indexOf("(") + 1, numText.indexOf(")")).toInt()
                    val listNode = nodeInfo.findAccessibilityNodeInfosByViewId("android:id/list")[0]
                    if (memberCount > 100) {
                        listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                        listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    }
                    val scrollNodeInfo = rootInActiveWindow
                    if (scrollNodeInfo != null) {
                        handler.postDelayed({
                            val nodes = scrollNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/e3x")
                            for (info in nodes) {
                                if (info.contentDescription.toString() == "添加成员") {
                                    info.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                    break
                                }
                            }
                        }, 1000L)
                    }
                }
            }
        } else {
            performBackClick()
        }

    }

    //5.添加成员
    private fun addMembers() {
        var members = arrayListOf<String>()
        //最后一次的时候清空记录，并且点击顶部确定按钮
        when (curGroup) {
            Constant.GROUP_NAME_1 -> members = mMember.python_1
            Constant.GROUP_NAME_2 -> members = mMember.python_2
            Constant.GROUP_NAME_3 -> members = mMember.android
            Constant.GROUP_NAME_5 -> members = mMember.guy
        }
        if (members.size > 0) {
            for (i in 0 until members.size) {
                handler.postDelayed({
                    val nodeInfo = rootInActiveWindow
                    if (nodeInfo != null) {
                        val editNodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b98")
                        if (editNodes != null && editNodes.size > 0) {
                            val editNode = editNodes[0]
                            val arguments = Bundle()
                            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, members[i])
                            editNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                        }
                    }
                }, 500L * (i + 1))
                handler.postDelayed({
                    val cbNodes = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/qm")
                    if (cbNodes != null) {
                        val cbNode: AccessibilityNodeInfo?
                        if (cbNodes.size > 0) {
                            cbNode = cbNodes[0]
                            cbNode?.parent?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        }
                    }
                    //最后一次的时候清空记录，并且点击顶部确定按钮
                    if (i == members.size - 1) {
                        val m = Hawk.get<Member>(Constant.MEMBER)
                        when (curGroup) {
                            Constant.GROUP_NAME_1 -> m.python_1 = arrayListOf()
                            Constant.GROUP_NAME_2 -> m.python_2 = arrayListOf()
                            Constant.GROUP_NAME_3 -> m.android = arrayListOf()
                            Constant.GROUP_NAME_5 -> m.guy = arrayListOf()
                        }
                        Hawk.put(Constant.MEMBER, m)
                        curGroup = ""
                        val sureNodes = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ki")
                        if (sureNodes != null && sureNodes.size > 0) {
                            sureNodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        }

                    }
                }, 700L * (i + 1))
            }
        }
    }

    //对话框自动点击
    private fun dialogClick() {
        val inviteNode = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b00")[0]
        inviteNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    //自动点赞
    private fun autoZan() {
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            while (true) {
                val rootNode = rootInActiveWindow
                if (rootNode != null) {
                    val listNodes = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/eii")
                    if (listNodes != null && listNodes.size > 0) {
                        val listNode = listNodes[0]
                        val zanNodes = listNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/eho")
                        for (zan in zanNodes) {
                            zan.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            Thread.sleep(300)
                            val zsNodes = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/eh_")
                            Thread.sleep(300)
                            if (zsNodes != null && zsNodes.size > 0) {
                                if (zsNodes[0].findAccessibilityNodeInfosByText("赞").size > 0) {
                                    zsNodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                }
                            }
                            Thread.sleep(500)
                        }
                        listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    }
                } else {
                    break
                }
            }
        }
    }

    //遍历控件的方法
    fun recycle(info: AccessibilityNodeInfo) {
        if (info.childCount == 0) {
            Log.i(TAG, "child widget----------------------------" + info.className.toString())
            Log.i(TAG, "showDialog:" + info.canOpenPopup())
            Log.i(TAG, "Text：" + info.text)
            Log.i(TAG, "windowId:" + info.windowId)
            Log.i(TAG, "desc:" + info.contentDescription)
        } else {
            (0 until info.childCount)
                    .filter { info.getChild(it) != null }
                    .forEach { recycle(info.getChild(it)) }
        }
    }


    //遍历获得未打开红包
    private fun openRedPacket() {
        val rootNode = rootInActiveWindow
        if (rootNode != null) {
            val listNode = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ag")
            if (listNode != null && listNode.size > 0) {
                val msgNodes = listNode[0].findAccessibilityNodeInfosByViewId("com.tencent.mm:id/atb")
                if (msgNodes != null && msgNodes.size > 0) {
                    for (rpNode in msgNodes) {
                        val rpStatusNode = rpNode.findAccessibilityNodeInfosByText("已领取")
                        val rpSecondStatusNode=rpNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/aw9")
//                        if (rpStatusNode != null && rpStatusNode.size > 0) {
//                            rpNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
//                            break
//                        }
                        if (rpStatusNode != null && rpStatusNode.size != 0) {
                            continue
                        }
                        if (rpSecondStatusNode!=null &&rpSecondStatusNode.size!=0){
                            continue
                        }
                        rpNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
            }
        }
    }

    //打开红包
    private fun clickRedPacket() {
        val nodeInfo = rootInActiveWindow
        val clickNode = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/dan")
        if (clickNode != null && clickNode.size > 0) {
            clickNode[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            performBackClick()
        }
    }

    private fun performBackClick() {
        handler.postDelayed({ performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) }, 1300L)
        Log.e(TAG, "点击返回")
    }

}