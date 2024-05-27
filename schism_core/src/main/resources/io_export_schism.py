bl_info = {
    "name":         "Export Schism Armature (Minecraft mod)",
    "author":       "Lycanite (Richard Nicholson)",
    "blender":      (2,8,0),
    "version":      (0,0,2),
    "location":     "File > Export > Schism Armature (.armature.json)",
    "description":  "Exports object data into an animation armature json file for loading into Schism mods for Minecraft.",
    "category":     "Import-Export"
}

import bpy
from bpy_extras.io_utils import ExportHelper
from bpy.props import *

class ExportSchism(bpy.types.Operator, ExportHelper):
  bl_idname = "export.schism"
  bl_label = 'Export Schism'
  
  # ExportHelper Mixin:
  filename_ext = "_parts.json"
  filter_glob : StringProperty(
            default="*_parts.json",
            options={'HIDDEN'},
  )
  
  filepath = StringProperty(name="File Path", description="Filepath for exporting", maxlen= 2048, default="")
  
  def execute(self, context):
    out = open(self.properties.filepath, "w")
    objects = bpy.context.scene.objects
    first = True
    out.write('"bones": {\n')
    out.write('    [\n')
    for obj in objects:
      if obj.type == 'MESH':
        if not first:
          out.write(',\n')
        first = False
        out.write('        {\n')
        out.write('            "name": "' + obj.name + '",\n')
        parentName = ''
        if obj.parent != None and obj.parent.type == 'MESH':
          parentName = obj.parent.name
        out.write('            "parent": "' + parentName + '",\n')
        out.write('            "x": "' + str(round(obj.matrix_world.to_translation()[0], 3)) + '",\n')
        out.write('            "y": "' + str(round(obj.matrix_world.to_translation()[2], 3)) + '",\n')
        out.write('            "z": "' + str(round(-obj.matrix_world.to_translation()[1], 3)) + '",\n')
        out.write('            "rotation_x": "' + str(round(eulerToDegree(obj.matrix_world.to_euler('XYZ')[0]), 3)) + '",\n')
        out.write('            "rotation_y": "' + str(round(eulerToDegree(obj.matrix_world.to_euler('XYZ')[2]), 3)) + '",\n')
        out.write('            "rotation_z": "' + str(round(eulerToDegree(obj.matrix_world.to_euler('XYZ')[1]), 3)) + '"\n')
        out.write('        }')
    out.write('    \n]\n')
    out.write('}')
    return {'FINISHED'}
  
  def invoke(self, context, event):
    self.filepath = ""
    wm = context.window_manager
    wm.fileselect_add(self)
    return {'RUNNING_MODAL'}


def menu_func(self, context):
  self.layout.operator(ExportSchism.bl_idname, text="Schism", icon='EXPORT')

#register, unregister = bpy.utils.register_classes_factory((ExportSchism))  
def register():
  bpy.utils.register_class(ExportSchism)
  bpy.types.TOPBAR_MT_file_export.append(menu_func)

def unregister():
  bpy.utils.unregister_class(ExportSchism)
  bpy.types.TOPBAR_MT_file_export.remove(menu_func)

def eulerToDegree(euler):
  pi = 22.0/7.0
  return ( (euler) / (2 * pi) ) * 360

if __name__ == "__main__":
  register()
