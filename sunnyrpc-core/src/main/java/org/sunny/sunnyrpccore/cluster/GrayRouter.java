package org.sunny.sunnyrpccore.cluster;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.sunny.sunnyrpccore.api.Router;
import org.sunny.sunnyrpccore.meta.InstanceMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 灰度路由.
 * // 可以做一些灰度用户，某一次请求上加灰度标记
 * // 结合蓝绿：
 * // 100 都是normal
 * // 100 都是灰度
 */

@Slf4j
@Data
public class GrayRouter implements Router<InstanceMeta> {

    private int grayRatio;
    private final Random random = new Random();

    public GrayRouter(int grayRatio) {
        this.grayRatio = grayRatio;
    }

    @Override
    public List<InstanceMeta> route(List<InstanceMeta> providers) {
        // ctx.gray=true
        if(providers==null||providers.size()<=1) {
            return providers;
        }

        List<InstanceMeta> normalNodes = new ArrayList<>();
        List<InstanceMeta> grayNodes = new ArrayList<>();

        providers.forEach(p->{
            if("true".equals(p.getParameters().get("gray"))) {
                grayNodes.add(p);
            } else {
                normalNodes.add(p);
            }
        });

        log.debug(" grayRouter grayNodes/normalNodes,grayRatio ===> {}/{},{}",
                grayNodes.size(), normalNodes.size(), grayRatio);

        if(normalNodes.isEmpty() || grayNodes.isEmpty()) return providers;
        if(grayRatio<=0) {
            return normalNodes;
        } else if(grayRatio >= 100) {
            return grayNodes;
        }

        if(random.nextInt(100) < grayRatio) {
            log.debug("use grayNodes ===> {}", grayNodes);
            return grayNodes;
        } else {
            log.debug("use normalNodes ===> {}", normalNodes);
            return normalNodes;
        }
    }
}
