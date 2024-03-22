package org.sunny.sunnyrpccore.registry;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.sunny.sunnyrpccore.meta.InstanceMeta;

import java.util.List;

@Data
@AllArgsConstructor
public class Event {
    List<InstanceMeta> data;
}
