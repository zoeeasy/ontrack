package net.nemerosa.ontrack.model.structure;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Wither;

import java.util.List;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ValidationStampFilter implements Entity {

    @Wither
    private final ID id;
    private final String name;
    @Wither
    private final Project project;
    @Wither
    private final Branch branch;
    @Wither
    private final List<String> vsNames;

}
