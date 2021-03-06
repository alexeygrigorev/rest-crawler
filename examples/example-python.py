import urllib
import requests

crawler_url = 'http://localhost:9811/crawl?js=true&urls=%s'

def crawl(urls):
    urls = [u.encode('unicode-escape') for u in urls]
    urls_encoded = urllib.quote(';'.join(urls))
    query_url = crawler_url % urls_encoded
    return requests.get(query_url).json()

urls = ['https://github.com/alexeygrigorev/rest-crawler', 
        'http://kaggle.com/', 'http://amazon.com/']
results = crawl(urls)
print results.keys()
