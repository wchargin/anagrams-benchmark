The English and Greek word lists are standard Scrabble word lists for
these two languages. I couldn’t find a word list for a language
contained outside the BMP, so I synthesized a Phoenician word list by
transliterating the Greek word list into the Phoenician script.

Specifically, `phoenician.txt` was created with the following Python 3
script (Python 2’s `chr` function is not as nice):

```py
# coding: utf-8

def translation_table():
  GREEK      = u'αβγδεζηθικλμνξοπρστ'
  PHOENECIAN = u'𐤀𐤁𐤂𐤃𐤄𐤆𐤇𐤈𐤉𐤊𐤋𐤌𐤍𐤎𐤏𐤐𐤑𐤔𐤕'
  assert len(GREEK) == len(PHOENECIAN), (len(GREEK), len(PHOENECIAN))

  identicals = zip('άέήίϊΐός', 'αεηιιιοσ')
  missing = 'υύϋΰφχψωώ'  # no Phoenecian equivalents

  result = {}
  for m in missing:
    result[m] = u''
  for (g, p) in zip(GREEK, PHOENECIAN):
    result[g] = p
  for (x, y) in identicals:
    result[x] = result[y]

  return result


def main():
  with open('greek.txt') as infile:
    lines = [line.lower().strip() for line in infile.readlines()]
  TRANSLATION_TABLE = translation_table()
  translated = [u''.join(TRANSLATION_TABLE[c] for c in x) for x in lines]
  with open('phoenician.txt', 'w') as outfile:
    outfile.write(u'\n'.join(translated))
    outfile.write(u'\n')


if __name__ == '__main__':
  main()
```
