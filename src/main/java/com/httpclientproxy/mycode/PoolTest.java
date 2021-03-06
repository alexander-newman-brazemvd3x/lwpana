package com.httpclientproxy.mycode;

import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Tianjinjin on 2016/11/2.
 */
public class PoolTest {

    public void test(CloseableHttpClient  httpClient,HttpRequestBase httpRequestBase){
        HttpHost proxy = new HttpHost("115.236.7.179", 3128);
        RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
        httpRequestBase.setConfig(config);

    }

    private  static  void  config(HttpRequestBase httpRequestBase)  {
        httpRequestBase.setHeader("User-Agent",  "Mozilla/5.0");
        httpRequestBase.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        httpRequestBase.setHeader("Accept-Language",  "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");//"en-US,en;q=0.5");
        httpRequestBase.setHeader("Accept-Charset", "ISO-8859-1,utf-8,gbk,gb2312;q=0.7,*;q=0.7");

        //  ???????????????????????????
        RequestConfig requestConfig  =  RequestConfig.custom()
                .setConnectionRequestTimeout(3000)
                .setConnectTimeout(3000)
                .setSocketTimeout(3000)
                .build();
        httpRequestBase.setConfig(requestConfig);
    }
    public  static  void  main(String[]  args)  {
        ConnectionSocketFactory plainsf  =  PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslsf  =  SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry  =  RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http",  plainsf)
                .register("https",  sslsf)
                .build();
        PoolingHttpClientConnectionManager cm  =  new  PoolingHttpClientConnectionManager(registry);
        //  ???????????????????????????200
        cm.setMaxTotal(200);
        //  ???????????????????????????????????????20
        cm.setDefaultMaxPerRoute(20);
        //  ??????????????????????????????????????????50
        HttpHost localhost  =  new  HttpHost("http://blog.csdn.net/gaolu",80);
        cm.setMaxPerRoute(new HttpRoute(localhost),  50);

        //??????????????????
        HttpRequestRetryHandler httpRequestRetryHandler  =  new  HttpRequestRetryHandler()  {
            public  boolean  retryRequest(IOException exception,int  executionCount,  HttpContext context)  {
                if  (executionCount  >=  5)  {//  ?????????????????????5???????????????
                    return  false;
                }
                if  (exception  instanceof NoHttpResponseException)  {//  ????????????????????????????????????????????????
                    return  true;
                }
                if  (exception  instanceof SSLHandshakeException)  {//  ????????????SSL????????????
                    return  false;
                }
                if  (exception  instanceof InterruptedIOException)  {//  ??????
                    return  false;
                }
                if  (exception  instanceof UnknownHostException)  {//  ????????????????????????
                    return  false;
                }
                if  (exception  instanceof ConnectTimeoutException)  {//  ???????????????
                    return  false;
                }
                if  (exception  instanceof SSLException)  {//  ssl????????????
                    return  false;
                }

                HttpClientContext  clientContext  =  HttpClientContext.adapt(context);
                HttpRequest request  =  clientContext.getRequest();
                //  ??????????????????????????????????????????
                if  (!(request  instanceof HttpEntityEnclosingRequest))  {
                    return  true;
                }
                return  false;
            }
        };

        CloseableHttpClient httpClient  =  HttpClients.custom()
                .setConnectionManager(cm)
                .setRetryHandler(httpRequestRetryHandler)
                .build();
        //  URL????????????
        String[]  urisToGet  =  {
                "http://blog.csdn.net/gaolu/article/details/48466059",
                "http://blog.csdn.net/gaolu/article/details/48243103",
                "http://blog.csdn.net/gaolu/article/details/47656987",
                "http://blog.csdn.net/gaolu/article/details/47055029",

                "http://blog.csdn.net/gaolu/article/details/46400883",
                "http://blog.csdn.net/gaolu/article/details/46359127",
                "http://blog.csdn.net/gaolu/article/details/46224821",
                "http://blog.csdn.net/gaolu/article/details/45305769",

                "http://blog.csdn.net/gaolu/article/details/43701763",
                "http://blog.csdn.net/gaolu/article/details/43195449",
                "http://blog.csdn.net/gaolu/article/details/42915521",
                "http://blog.csdn.net/gaolu/article/details/41802319",

                "http://blog.csdn.net/gaolu/article/details/41045233",
                "http://blog.csdn.net/gaolu/article/details/40395425",
                "http://blog.csdn.net/gaolu/article/details/40047065",
                "http://blog.csdn.net/gaolu/article/details/39891877",

                "http://blog.csdn.net/gaolu/article/details/39499073",
                "http://blog.csdn.net/gaolu/article/details/39314327",
                "http://blog.csdn.net/gaolu/article/details/38820809",
                "http://blog.csdn.net/gaolu/article/details/38439375",
        };

        long  start  =  System.currentTimeMillis();
        try  {
            int  pagecount  =  urisToGet.length;
            ExecutorService executors  =  Executors.newFixedThreadPool(pagecount);
            CountDownLatch countDownLatch  =  new  CountDownLatch(pagecount);
            for(int  i  =  0;  i<  pagecount;i++){
                HttpGet httpget  =  new  HttpGet(urisToGet[i]);
                config(httpget);
                //??????????????????
                executors.execute(new  GetRunnable(httpClient,httpget,countDownLatch));
            }
            countDownLatch.await();
            executors.shutdown();
        }  catch  (InterruptedException  e)  {
            e.printStackTrace();
        }  finally  {
            System.out.println("??????"  +  Thread.currentThread().getName()  +  ","  +  System.currentTimeMillis()  +  ",  ????????????????????????????????????????????????");
        }

        long  end  =  System.currentTimeMillis();
        System.out.println("consume  ->  "  +  (end  -  start));
    }

    static  class  GetRunnable  implements  Runnable  {
        private  CountDownLatch  countDownLatch;
        private  final  CloseableHttpClient  httpClient;
        private  final  HttpGet  httpget;

        public  GetRunnable(CloseableHttpClient  httpClient,  HttpGet  httpget,  CountDownLatch  countDownLatch){
            this.httpClient  =  httpClient;
            this.httpget  =  httpget;

            this.countDownLatch  =  countDownLatch;
        }

        public  void  run()  {
            CloseableHttpResponse response  =  null;
            try  {
                response  =  httpClient.execute(httpget,HttpClientContext.create());
                HttpEntity entity  =  response.getEntity();
                System.out.println(EntityUtils.toString(entity, "utf-8"))  ;
                EntityUtils.consume(entity);
            }  catch  (IOException  e)  {
                e.printStackTrace();
            }  finally  {
                countDownLatch.countDown();

                try  {
                    if(response  !=  null)
                        response.close();
                }  catch  (IOException  e)  {
                    e.printStackTrace();
                }
            }
        }
    }
}
