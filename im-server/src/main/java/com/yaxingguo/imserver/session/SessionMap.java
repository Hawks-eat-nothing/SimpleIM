package com.yaxingguo.imserver.session;

import com.yaxingguo.imcommon.bean.User;
import com.yaxingguo.util.Logger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Slf4j
@Data
public class SessionMap {

    private static SessionMap singleInstance = new SessionMap();
    private ConcurrentMap<String,ServerSession> map = new ConcurrentHashMap<>();

    public static SessionMap inst() {
        return singleInstance;
    }

    /**
     * 增加session对象
     */
    public void addSession( ServerSession s) {
        map.put(s.getSessionId(), s);
        log.info("用户登录:id= " + s.getUser().getUid()
                + "   在线人数: " + map.size());

    }
    //获取会话对象
    public ServerSession getSession(String sessionId){
        if (map.containsKey(sessionId)){
            return map.get(sessionId);
        }else {
            return null;
        }
    }
    //删除会话
    public void removeSession(String sessionId){
        if (!map.containsKey(sessionId)){
            return;
        }
        ServerSession s = map.get(sessionId);
        map.remove(sessionId);
        log.info("用户下线:id= "+s.getUser().getUid()+" 在线人数: "+map.size());
    }
    public boolean hasLogin(User user) {
        Iterator<Map.Entry<String, ServerSession>> it =
                map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ServerSession> next = it.next();
            User u = next.getValue().getUser();
            if (u.getUid().equals(user.getUid())
                    && u.getPlatform().equals(user.getPlatform())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 根据用户id，获取session对象
     * 由于一个用户可能有多个会话，因此需要调用SessionMap会话管理器的
     * SessionMap.inst().getSessionBy(String)方法来取得这个用户的所有会话
     */
    public List<ServerSession> getSessionsBy(String userId) {

        List<ServerSession> list = map.values()
                .stream()
                .filter(s -> s.getUser().getUid().equals(userId))
                .collect(Collectors.toList());
        return list;
    }

}
