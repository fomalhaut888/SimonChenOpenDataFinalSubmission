shinyUI(fluidPage(
  titlePanel("Daily Data Query"),
  sidebarLayout(
    sidebarPanel(
      uiOutput("customer"),
      uiOutput("campaign"),
      selectInput("pointer", label = h4("Select pointer"), 
                  choices=c("clicks", "impressions", "ctr", "conversions","conv_rate","cost"))
    ),
    mainPanel(plotOutput("distPlot"))
  ),
  list(
    ui = basicPage(
      dataTableOutput('daily_data')
    )
  )
))