import json
import os
import io
from itertools import izip, islice
from os.path import join

from PIL import Image


root = 'src/main/resources/assets/'
android_icons_url = 'http://www.androidicons.com/'
material_icons_url = 'https://www.google.com/design/icons/'

def convert_image(file):
    pil_img = Image.open(file)
    if pil_img.mode == '1':
        img = Image.open(file)
        img = img.convert('RGBA')
        datas = img.getdata()
        newData = []
        for item in datas:
            if item[0] == 0 and item[1] == 0 and item[2] == 0:
                newData.append((0, 0, 0, 0))
            else:
                newData.append(item)

        img.putdata(newData)
        img.save(file)

def convert_images():
    iconpack_root = join(root, 'android_icons')
    for color in list_dirs_only(iconpack_root):
        color_root = join(iconpack_root, color)
        for resolution in list_dirs_only(color_root):
            resolution_root = join(color_root, resolution)
            for image in list_images(resolution_root):
                convert_image(join(resolution_root, image))

    iconpack_root = join(root, 'material_icons')
    for category in list_dirs_only(iconpack_root):
        category_root = join(iconpack_root, category)
        for resolution in list_dirs_only(category_root):
            resolution_root = join(category_root, resolution)
            for image in list_images(resolution_root):
                convert_image(join(resolution_root, image))


def get_android_icons_file(color, resolution, name):
    iconpack_root = join(root, 'android_icons')
    color_root = join(iconpack_root, color)
    resolution_root = join(color_root, resolution)
    full_name = name + '.png'
    return join(resolution_root, full_name)


def handle_android_icons():
    iconpack_root = join(root, 'android_icons')
    id = 'android_icons'
    colors = list_dirs_only(iconpack_root)
    firstcolordir = join(iconpack_root, colors[0])
    resolutions = list_dirs_only(firstcolordir)
    assets = list_images(join(firstcolordir, resolutions[0]))
    packdata = {'name': 'Android Icons',
                'id': id,
                'url': android_icons_url,
                'path': 'android_icons/',
                'categories': ['all']
                }
    assetdata = []
    for asset in assets:
        name = os.path.splitext(asset)[0]
        data = {'name': name,
                'pack': id,
                'category': 'all',
                'resolutions': resolutions,
                'colors': colors,
                'sizes': ['18dp']}
        assetdata.append(data)

    packdata['assets'] = assetdata
    return packdata


def list_dirs_only(dir):
    return [f for f in os.listdir(dir) if os.path.isdir(join(dir, f))]


def list_images(dir):
    return [f for f in os.listdir(dir) if f.endswith('.png')]


def extract_data(icon_file_name):
    file_name = os.path.splitext(icon_file_name)[0]
    splits = file_name.split('_')
    size = splits[-1]
    splits.pop()
    color = splits[-1]
    splits.pop()
    name = '_'.join(splits)
    return name, color, size


def get_material_icons_file(category, color, size, resolution, name):
    iconpack_root = join(root, 'material_icons')
    category_root = join(iconpack_root, category)
    resolution_root = join(category_root, 'drawable-' + resolution)
    full_name = "_".join([name, color, size]) + '.png'
    return join(resolution_root, full_name)


def handle_material_icons():
    iconpack_root = join(root, 'material_icons')
    categories = list_dirs_only(iconpack_root)
    id = 'material_icons'
    packdata = {'name': 'Material Icons',
                'id': id,
                'url': material_icons_url,
                'path': 'material_icons/',
                'categories': categories
                }
    assetdata = []
    for category in categories:
        category_root = join(iconpack_root, category)
        resolutions = [s.split('-')[1] for s in list_dirs_only(category_root)]
        raw_assets = [extract_data(f) for f in list_images(join(category_root, 'drawable-' + resolutions[0]))]
        colors = []
        sizes = []
        for current_item, next_item in izip(raw_assets, islice(raw_assets, 1, None)):
            colors.append(current_item[1])
            sizes.append(current_item[2])
            if current_item[0] != next_item[0]:
                data = {'name': current_item[0],
                        'pack': id,
                        'category': category,
                        'resolutions': resolutions,
                        'colors': sorted(set(colors)),
                        'sizes': sorted(set(sizes))}
                assetdata.append(data)
                colors = []
                sizes = []
                continue

    packdata['assets'] = assetdata
    return packdata


convert_images()
android_icons_data = handle_android_icons()
material_icons_data = handle_material_icons()
packs = [android_icons_data, material_icons_data]

with io.open(join(root, 'content.json'), 'w', encoding='utf-8') as f:
    f.write(unicode(json.dumps(packs, ensure_ascii=False)))

print('Created json at ' + join(join(os.getcwd(), root), 'content.json'))