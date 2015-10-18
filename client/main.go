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
	tr := &http.Transport{
		Dial: (&net.Dialer{
			Timeout:   3600 * time.Second,
			KeepAlive: 3600 * time.Second,
		}).Dial,
		MaxIdleConnsPerHost: 1000000,
	}
	for i := 0; ; i += 100 {
		clients := make([]*http.Client, 0, 100)
		for c := 0; c < 100; c++ {
			clients = append(clients, &http.Client{Transport: tr})
		}
		go get(clients, i, 0)
		if i%100 == 0 {
			fmt.Printf("Started %v sleeping\n", i)
		}
	}
	time.Sleep(1 * time.Millisecond)
}

func get(clients []*http.Client, no, i int) {
	for _, cl := range clients {
		response, err := cl.Get(url)
		if err != nil {
			log.Panic(err)
		}
		// Verify if the response was ok
		if response.StatusCode != http.StatusOK {
			log.Println(response.Status)
		}
		defer response.Body.Close()
		io.Copy(ioutil.Discard, response.Body)
	}
	time.Sleep(5000 * time.Millisecond)
	get(clients, no, i+1)
}
