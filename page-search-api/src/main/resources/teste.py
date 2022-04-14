import sys
import yake
import json
from newspaper import Article
from newspaper import fulltext


extractor = yake.KeywordExtractor()

# print("This is the name of the program:", sys.argv[0])
# print("This is the first argument:", sys.argv[1])

url = sys.argv[1]
article = Article(url)
article.download()

text = ""
try:
    text = fulltext(article.html)
except Exception as e:
    text = article.text

extractedKeywords = extractor.extract_keywords(text)

output = {
  "newspaper3kExtractedText": text,
  "yakeExtractedKeywords": extractedKeywords
}

print(json.dumps(output))

