package protocol

import (
      "io"
      "fmt"
      "math"
)

const (
      R = 6371.0 // Km.
      MAXDISTANCE = 10 // Km.
)

type Location struct {
      ltd float64
      lng float64
      ratio int32
}

func NewLocationFillOut(ltd, lng float64, r int32) *Location {
      return &Location{ltd, lng, r}
}

func NewLocation() *Location {
      return &Location{}
}

func (loc *Location) Write(tx io.Writer) {
      WriteFloat(tx, loc.ltd)
      WriteFloat(tx, loc.lng)
      WriteInt32(tx, loc.ratio)
}

func (loc *Location) Read(rx io.Reader) {
      loc.ltd = ReadFloat(rx)
      loc.lng = ReadFloat(rx)
      loc.ratio = ReadInt32(rx)
}

func (loc Location) String() string {
      return fmt.Sprintf("Latitude : %f\nLongitude : %f\nRatio : %d \n",
            loc.ltd, loc.lng, loc.ratio)
}

func (loc *Location) Equals(l *Location) bool {
      eq := math.Abs(loc.ltd - l.ltd) < 0.1
      eq = eq && math.Abs(loc.lng - l.lng) < 0.1
      eq = eq && loc.ratio == l.ratio;

      return eq
}

func (loc Location) FileFormat() string {
      return fmt.Sprintf("%f,%f,%d", loc.ltd, loc.lng, loc.ratio)
}

func (loc Location) Distance(l Location) float64 {
      radLtd1 := getRad(loc.ltd)
      radLtd2 := getRad(l.ltd)

      incLtd := getRad(loc.ltd - l.ltd)
      incLng := getRad(loc.lng - l.lng)

      a := math.Pow(math.Sin(incLtd / 2.0) , 2) +
                  math.Cos(radLtd1) * math.Cos(radLtd2) *
                  math.Pow(math.Sin(incLng / 2.0), 2)

      c := 2 * math.Atan2(math.Sqrt(a), math.Sqrt(1 - a))

      return R * c
}

func (loc Location) IsMaxDistance(l Location) bool {
      return loc.Distance(l) > MAXDISTANCE
}

func getRad(degrees float64) float64 {
      return degrees * math.Pi / 180.0
}
