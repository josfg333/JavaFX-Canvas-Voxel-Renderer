module josfg333.voxel.software_render {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires com.almasb.fxgl.all;

    opens josfg333.voxel.software_render to javafx.fxml;
    exports josfg333.voxel.software_render;
}