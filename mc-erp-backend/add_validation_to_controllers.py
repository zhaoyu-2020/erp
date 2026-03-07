from pathlib import Path
import re

ctrl_dir = Path(__file__).resolve().parent / 'src' / 'main' / 'java' / 'com' / 'mc' / 'erp' / 'controller'
updated_files = []
for path in ctrl_dir.glob('*.java'):
    text = path.read_text(encoding='utf-8')
    if '@RequestBody' not in text:
        continue
    orig = text
    # Ensure imports exist
    if 'import jakarta.validation.Valid;' not in text:
        imports = list(re.finditer(r'^(import .+;)$', text, re.MULTILINE))
        if imports:
            last = imports[-1]
            insert_pos = last.end()
            text = text[:insert_pos] + '\nimport jakarta.validation.Valid;' + text[insert_pos:]
    if 'import org.springframework.validation.annotation.Validated;' not in text:
        imports = list(re.finditer(r'^(import .+;)$', text, re.MULTILINE))
        if imports:
            last = imports[-1]
            insert_pos = last.end()
            text = text[:insert_pos] + '\nimport org.springframework.validation.annotation.Validated;' + text[insert_pos:]
    # Ensure @Validated on class
    if '@RestController' in text and '@Validated' not in text:
        # Insert @Validated right after @RestController
        text = text.replace('@RestController\n', '@RestController\n@Validated\n', 1)
    # Add @Valid before @RequestBody if missing
    text = re.sub(r'(?<!@Valid\s)(@RequestBody)', r'@Valid \1', text)
    if text != orig:
        path.write_text(text, encoding='utf-8')
        updated_files.append(str(path))

print('Updated', len(updated_files), 'files:')
for p in updated_files:
    print(' -', p)
