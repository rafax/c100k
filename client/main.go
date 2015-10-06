package main

import (
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"net"
	"net/http"
	"time"
)

var url = "http://localhost:8080"

func main() {
	for i := 0; ; i++ {
		tr := &http.Transport{
			Dial: (&net.Dialer{
				Timeout:   3600 * time.Second,
				KeepAlive: 3600 * time.Second,
			}).Dial,
			MaxIdleConnsPerHost: 1000000,
		}
		var started = make(chan struct{})
		cl := &http.Client{Transport: tr}
		go get(cl, started, i, 0)
		<-started
		if i%100 == 0 {
			fmt.Printf("Started %v sleeping\n", i)
		}
		time.Sleep(1 * time.Millisecond)
	}
}

func get(cl *http.Client, started chan struct{}, no, i int) {
	response, err := cl.Get(url)
	if err != nil {
		log.Panic(err)
	}
	if i == 0 {
		started <- struct{}{}
	}
	// Verify if the response was ok
	if response.StatusCode != http.StatusOK {
		log.Println(response.Status)
	}
	defer response.Body.Close()
	io.Copy(ioutil.Discard, response.Body)
	//log.Printf("#%v: %v\n", no, i)
	time.Sleep(1000 * time.Millisecond)
	get(cl, started, no, i+1)
}
