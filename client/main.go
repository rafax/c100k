package main

import (
	"flag"
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"net"
	"net/http"
	"sync"
	"time"
)

const perRoutine int = 100
const total int = 1000
const roundtrips int = 10
const tripPause time.Duration = time.Duration(1000) * time.Millisecond

var addr = flag.String("address", "127.0.0.1", "address to bind to OR beginning of ip range")
var port = flag.Int("port", 8098, "port to connect to")
var ipRangeStart = flag.Int("ip-range-start", 200, "start of ip-range")
var ipRangeEnd = flag.Int("ip-range-end", 230, "end of ip-range")

func main() {
	flag.Parse()

	var wg sync.WaitGroup
	for i := 0; i < total; i += perRoutine {
		clients := make([]*http.Client, 0, perRoutine)
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
		go get(clients, i, 0, &wg)
		if i%(10*perRoutine) == 0 {
			fmt.Printf("Started %v sleeping\n", i)
		}
		time.Sleep(1 * time.Millisecond)
	}
	wg.Wait()
	fmt.Println("Done")
}

func get(clients []*http.Client, no, i int, wg *sync.WaitGroup) {
	url := fmt.Sprintf("http://%v:%v", *addr, *port)
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
	time.Sleep(tripPause)
	if i == roundtrips {
		wg.Done()
		fmt.Println("Exiting, no", no)
		return
	}
	get(clients, no, i+1, wg)
}
