#Use ODBC
library(RODBC)
#Use DBI
#library(DBI)
#library(RMySQL)
library(ggplot2)
library(plyr)
shinyServer(function(input, output) {
  #datasetInput <- reactive({
    
  #})
  output$customer <- renderUI({
    sql2 <- "select distinct a.customer, b.name from daily_data a, customer b where a.customer = b.id"
    #Use ODBC
    myconn <- odbcConnect("OpenDataDS")
    customer_data <- sqlQuery(myconn, sql2)
    close(myconn)
    #Use DBI
    #driver <- dbDriver("MySQL")
    #myconn <- dbConnect(driver, host="127.0.0.1", port=3306, user="root", password="01517124", dbname="ADWORDSDB")
    #customer_data <- dbGetQuery(myconn, sql2)
    #dbDisconnect(myconn) 
    customer_list <- dlply(customer_data,.(name),function(f){ return (f$customer)})
    selectInput("customer", label = h4("Select customer"), choices=customer_list)
  })
  
  output$campaign <- renderUI({
    sql3 <- paste("select distinct campaign_name from daily_data a where a.customer = ",
                  input$customer,sep="")
    #Use ODBC
    myconn <- odbcConnect("OpenDataDS")
    campaign_data <- sqlQuery(myconn, sql3)
    close(myconn)
    #Use DBI
    #driver <- dbDriver("MySQL")
    #myconn <- dbConnect(driver, host="127.0.0.1", port=3306, user="root", password="01517124", dbname="ADWORDSDB")
    #campaign_data <- dbGetQuery(myconn, sql3)
    #dbDisconnect(myconn)
    campaign_list <- dlply(campaign_data,.(campaign_name),function(f){ return (f$campaign_name)})
    selectInput("campaign", label = h4("Select campaign"), choices=campaign_list)
  })
  
  datasetInput <- reactive({
    sql1 <- paste("select data_date,",input$pointer," from daily_data where campaign_name='",
                  input$campaign, "' order by data_date",
                  sep="")
    #Use ODBC
    myconn <- odbcConnect("OpenDataDS")
    daily_data <- sqlQuery(myconn, sql1)
    close(myconn)
    #Use DBI
    #driver <- dbDriver("MySQL")
    #myconn <- dbConnect(driver, host="127.0.0.1", port=3306, user="root", password="01517124", dbname="ADWORDSDB")
    #daily_data <- dbGetQuery(myconn, sql1)
    #dbDisconnect(myconn)
    #daily_data$data_date <- as.POSIXct(daily_data$data_date)
    daily_data
  })
  
  output$daily_data <- renderDataTable({
    dataset <- datasetInput()
    dataset
  })
  
  output$distPlot <- renderPlot({
    dataset <- datasetInput()
    pointer <- input$pointer
    data_date <- dataset$data_date
    if(pointer == 'clicks'){
      clicks <- dataset[,2]
      daily_data2 <- data.frame(clicks,data_date)
      co <- aes(x=date, y=clicks)
    }else if(pointer == 'impressions'){
      impressions <- dataset[,2]
      daily_data2 <- data.frame(impressions,data_date)
      co <- aes(x=date, y=impressions)
    }else if(pointer == 'ctr'){
      ctr <- dataset[,2]
      daily_data2 <- data.frame(ctr,data_date)
      co <- aes(x=date, y=ctr)
    }else if(pointer == 'conversions'){
      conversions <- dataset[,2]
      daily_data2 <- data.frame(conversions,data_date)
      co <- aes(x=date, y=conversions)
    }else if(pointer == 'conv_rate'){
      conv_rate <- dataset[,2]
      daily_data2 <- data.frame(conv_rate,data_date)
      co <- aes(x=date, y=conv_rate)
    }else if(pointer == 'cost'){
      cost <- dataset[,2]
      daily_data2 <- data.frame(cost,data_date)
      co <- aes(x=date, y=cost)
    }
    daily_data2$date <- as.numeric(format(dataset$data_date,'%d'))
    getColorByDayOfWeeks <- function(date){
      color <- c('skyblue','red','orange','green','blue','gray','purple')
      w <- as.integer(format(date, '%w')) + 1
      return (color[w])
    }
    fillcolor <- sapply(daily_data2$data_date, FUN=getColorByDayOfWeeks)
    p1 <- ggplot(daily_data2,co)+ 
      geom_histogram(stat="identity",
                     binwidth=1,
                     fill=fillcolor,
                     colour='black')
    p1
  })
})
