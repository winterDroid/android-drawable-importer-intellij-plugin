import json
import os
import io
from itertools import izip, islice
from os.path import join

root = 'src/main/assets/'
android_icons_url = 'http://www.androidicons.com/'
material_icons_url = 'https://www.google.com/design/icons/'


def handle_android_icons():
    iconpack_root = join(root, "android_icons")
    colors = os.listdir(iconpack_root)
    firstcolordir = join(iconpack_root, colors[0])
    resolutions = os.listdir(firstcolordir)
    assets = os.listdir(join(firstcolordir, resolutions[0]))
    packdata = {'name': 'Android Icons', 'url': android_icons_url, 'path': iconpack_root}
    assetdata = []
    for asset in assets:
        data = {'name': os.path.splitext(asset)[0],
                'category': 'all',
                'resolutions': resolutions,
                'colors': colors,
                'sizes': ['18dp']}
        assetdata.append(data)

    packdata['assets'] = assetdata
    return packdata


def extract_data(icon_file_name):
    file_name = os.path.splitext(icon_file_name)[0]
    splits = file_name.split('_')
    size = splits[-1]
    splits.pop()
    color = splits[-1]
    splits.pop()
    name = '_'.join(splits)
    return name, color, size


def handle_material_icons():
    iconpack_root = join(root, "material_icons")
    categories = os.listdir(iconpack_root)
    packdata = {'name': 'Material Icons', 'url': material_icons_url, 'path': iconpack_root}
    assetdata = []
    for category in categories:
        category_root = join(iconpack_root, category)
        resolutions = [s.split('-')[1] for s in os.listdir(category_root)]
        raw_assets = [extract_data(f) for f in os.listdir(join(category_root, 'drawable-' + resolutions[0]))]
        colors = []
        sizes = []
        for current_item, next_item in izip(raw_assets, islice(raw_assets, 1, None)):
            colors.append(current_item[1])
            sizes.append(current_item[2])
            if current_item[0] != next_item[0]:
                data = {'name': current_item[0],
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


android_icons_data = handle_android_icons()
material_icons_data = handle_material_icons()
packs = {'packs': [android_icons_data, material_icons_data]}

with io.open(join(root, 'content.json'), 'w', encoding='utf-8') as f:
    f.write(unicode(json.dumps(packs, ensure_ascii=False)))

print('Created json at ' + join(join(os.getcwd(), root), 'content.json'))