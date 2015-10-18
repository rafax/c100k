package main

import (
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"net"
	"net/http"
	"sync"
	"time"
)

var url = "http://localhost:8080"

const perRoutine int = 100
const total int = 10000
const roundtrips int = 100

func main() {
	var wg sync.WaitGroup
	for i := 0; i < total; i += perRoutine {
		clients := make([]*http.Client, 0, 100)
		for c := 0; c < perRoutine; c++ {
			tr := &http.Transport{
				Dial: (&net.Dialer{
					Timeout:   3600 * time.Second,
					KeepAlive: 3600 * time.Second,
				}).Dial,
				MaxIdleConnsPerHost: 1000000,
			}
			clients = append(clients, &http.Client{Transport: tr})
		}
		wg.Add(1)
		go get(clients, i, 0, wg)
		if i%100 == 0 {
			fmt.Printf("Started %v sleeping\n", i)
		}
		time.Sleep(1 * time.Millisecond)
	}
	wg.Wait()
}

func get(clients []*http.Client, no, i int, wg sync.WaitGroup) {
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
	if i == roundtrips {
		wg.Done()
		return
	}
	get(clients, no, i+1, wg)
}
